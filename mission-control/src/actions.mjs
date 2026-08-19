import fs from 'node:fs';
import path from 'node:path';
import { spawn } from 'node:child_process';
import { ROOT_DIR, BACKEND_URL, FRONTEND_URL, SERVICE_ORDER, PROCESS_SERVICES } from './config.mjs';
import { startProcess, stopProcess, isRunning, getPid, logFile } from './process-manager.mjs';
import { startDb, stopDb, dbHealth, isDbRunning } from './docker.mjs';
import { waitForHttp, waitForCondition } from './health.mjs';
import { openBrowser } from './open-browser.mjs';

// Shared orchestration logic used by both the CLI (src/cli.mjs) and the
// dashboard's HTTP API (src/server.mjs). Every action takes an `onMessage`
// callback instead of writing to console directly, so the CLI can print it
// and the dashboard can relay it to the browser.

export async function startOne(name, { open = false, onMessage = () => {} } = {}) {
  if (name === 'db') {
    if (isDbRunning()) {
      onMessage('db: already running');
    } else {
      onMessage('db: starting postgres container...');
      startDb();
    }
    const healthy = await waitForCondition(() => dbHealth() === 'healthy', {
      timeoutMs: 30_000,
      intervalMs: 1000,
    });
    onMessage(healthy ? 'db: healthy' : 'db: did not report healthy in time (check `docker compose logs postgres`)');
    return;
  }

  const svc = PROCESS_SERVICES[name];
  const { alreadyRunning, pid } = startProcess(name, svc);
  onMessage(
    alreadyRunning
      ? `${name}: already running (pid ${pid})`
      : `${name}: starting (pid ${pid}), logs at mission-control/.state/logs/${name}.log`,
  );

  const timeoutMs = name === 'backend' ? 120_000 : 45_000;
  const healthy = await waitForHttp(svc.healthUrl, { timeoutMs });
  if (healthy) {
    onMessage(`${name}: ${svc.readyMessage}`);
    if (open && name === 'frontend') {
      openBrowser(FRONTEND_URL);
    }
  } else {
    onMessage(`${name}: did not become healthy within ${timeoutMs / 1000}s - check its logs`);
  }
}

export async function startTarget(target, opts = {}) {
  const targets = target === 'all' ? SERVICE_ORDER : [target];
  if (targets.includes('backend') && !targets.includes('db')) {
    await startOne('db', opts);
  }
  for (const name of targets) {
    await startOne(name, opts);
  }
}

export function stopOne(name, { onMessage = () => {} } = {}) {
  if (name === 'db') {
    if (!isDbRunning()) {
      onMessage('db: not running');
      return;
    }
    onMessage('db: stopping postgres container...');
    stopDb();
    onMessage('db: stopped');
    return;
  }

  const { wasRunning } = stopProcess(name);
  onMessage(wasRunning ? `${name}: stopped` : `${name}: not running`);
}

export function stopTarget(target, opts = {}) {
  const targets = target === 'all' ? [...SERVICE_ORDER].reverse() : [target];
  for (const name of targets) {
    stopOne(name, opts);
  }
}

export async function restartTarget(target, opts = {}) {
  const targets = target === 'all' ? SERVICE_ORDER : [target];
  stopTarget(target === 'all' ? 'all' : target, opts);
  for (const name of targets) {
    await startOne(name, opts);
  }
}

export function statusRows() {
  const rows = [];

  const dbRunning = isDbRunning();
  rows.push({ service: 'db', state: dbRunning ? dbHealth() : 'stopped', pid: null, port: 5432 });

  for (const name of ['backend', 'frontend']) {
    const running = isRunning(name);
    rows.push({
      service: name,
      state: running ? 'running' : 'stopped',
      pid: running ? getPid(name) : null,
      port: name === 'backend' ? 8080 : 5173,
    });
  }

  return rows;
}

export function readLog(name) {
  const file = logFile(name);
  if (!fs.existsSync(file)) return null;
  return fs.readFileSync(file, 'utf8');
}

export function isPlaywrightInstalled() {
  return fs.existsSync(path.join(ROOT_DIR, 'frontend', 'node_modules', '@playwright', 'test'));
}

export function runE2e({ onData = () => {} } = {}) {
  return new Promise((resolve) => {
    const child = spawn('npm run test:e2e', {
      cwd: path.join(ROOT_DIR, 'frontend'),
      shell: true,
    });
    child.stdout.on('data', (chunk) => onData(chunk.toString()));
    child.stderr.on('data', (chunk) => onData(chunk.toString()));
    child.on('exit', (code) => resolve(code ?? 1));
  });
}

export function openTarget(target) {
  const name = target === 'backend' ? 'backend' : 'frontend';
  openBrowser(name === 'backend' ? BACKEND_URL : FRONTEND_URL);
  return name;
}
