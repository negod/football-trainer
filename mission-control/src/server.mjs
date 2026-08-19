import fs from 'node:fs';
import path from 'node:path';
import http from 'node:http';
import { fileURLToPath } from 'node:url';
import { DASHBOARD_PORT } from './config.mjs';
import { startTarget, stopTarget, restartTarget, statusRows, readLog, isPlaywrightInstalled, runE2e, openTarget } from './actions.mjs';
import { dbLogsText } from './docker.mjs';
import { openBrowser } from './open-browser.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const DASHBOARD_HTML_PATH = path.join(__dirname, 'dashboard.html');

const VALID_TARGETS = new Set(['db', 'backend', 'frontend', 'all']);

// Playwright can take a while; only one run at a time, tracked here so the
// dashboard can poll its progress instead of holding the HTTP request open.
const e2eState = { running: false, exitCode: null, output: '' };

function runE2eInBackground() {
  if (e2eState.running) return;
  e2eState.running = true;
  e2eState.exitCode = null;
  e2eState.output = '';

  (async () => {
    if (!isPlaywrightInstalled()) {
      e2eState.output = 'Playwright is not installed. Run:\n  npm install --prefix frontend\n  npx --prefix frontend playwright install --with-deps chromium\n';
      e2eState.exitCode = 1;
      e2eState.running = false;
      return;
    }

    await startTarget('all', {
      open: false,
      onMessage: (msg) => {
        e2eState.output += `[mission-control] ${msg}\n`;
      },
    });

    e2eState.output += '[mission-control] running Playwright E2E tests...\n';
    const exitCode = await runE2e({ onData: (chunk) => { e2eState.output += chunk; } });
    e2eState.exitCode = exitCode;
    e2eState.running = false;
  })();
}

function sendJson(res, status, body) {
  const payload = JSON.stringify(body);
  res.writeHead(status, { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(payload) });
  res.end(payload);
}

function readJsonBody(req) {
  return new Promise((resolve) => {
    let data = '';
    req.on('data', (chunk) => { data += chunk; });
    req.on('end', () => {
      try {
        resolve(data ? JSON.parse(data) : {});
      } catch {
        resolve({});
      }
    });
  });
}

async function handleAction(req, res, run) {
  const body = await readJsonBody(req);
  const target = body.target ?? 'all';
  if (!VALID_TARGETS.has(target)) {
    sendJson(res, 400, { error: `Unknown target "${target}"` });
    return;
  }

  const messages = [];
  try {
    await run(target, (msg) => messages.push(msg));
    sendJson(res, 200, { ok: true, messages });
  } catch (err) {
    sendJson(res, 500, { ok: false, messages, error: String(err?.message ?? err) });
  }
}

function requestListener(req, res) {
  const url = new URL(req.url, 'http://localhost');

  if (req.method === 'GET' && url.pathname === '/') {
    const html = fs.readFileSync(DASHBOARD_HTML_PATH, 'utf8');
    res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
    res.end(html);
    return;
  }

  if (req.method === 'GET' && url.pathname === '/api/status') {
    sendJson(res, 200, { services: statusRows(), e2e: { running: e2eState.running, exitCode: e2eState.exitCode } });
    return;
  }

  if (req.method === 'POST' && url.pathname === '/api/start') {
    handleAction(req, res, (target, onMessage) => startTarget(target, { open: false, onMessage }));
    return;
  }

  if (req.method === 'POST' && url.pathname === '/api/stop') {
    handleAction(req, res, (target, onMessage) => stopTarget(target, { onMessage }));
    return;
  }

  if (req.method === 'POST' && url.pathname === '/api/restart') {
    handleAction(req, res, (target, onMessage) => restartTarget(target, { open: false, onMessage }));
    return;
  }

  if (req.method === 'POST' && url.pathname === '/api/open') {
    readJsonBody(req).then((body) => {
      const name = openTarget(body.target);
      sendJson(res, 200, { ok: true, opened: name });
    });
    return;
  }

  if (req.method === 'POST' && url.pathname === '/api/e2e') {
    runE2eInBackground();
    sendJson(res, 202, { started: true });
    return;
  }

  if (req.method === 'GET' && url.pathname === '/api/e2e') {
    sendJson(res, 200, e2eState);
    return;
  }

  const logsMatch = url.pathname.match(/^\/api\/logs\/(db|backend|frontend)$/);
  if (req.method === 'GET' && logsMatch) {
    const service = logsMatch[1];
    const content = service === 'db' ? dbLogsText({ tail: 200 }) : readLog(service);
    sendJson(res, 200, { content: content ?? '(no log yet)' });
    return;
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' });
  res.end('Not found');
}

export function startDashboard({ port = DASHBOARD_PORT, open = false } = {}) {
  return new Promise((resolve, reject) => {
    const server = http.createServer(requestListener);
    server.on('error', reject);
    server.listen(port, () => {
      const url = `http://localhost:${port}`;
      console.log(`[mission-control] dashboard running at ${url} (Ctrl+C to stop)`);
      if (open) openBrowser(url);
      resolve(server);
    });
  });
}
