#!/usr/bin/env node
import fs from 'node:fs';
import { spawnSync } from 'node:child_process';
import { ROOT_DIR, FRONTEND_URL, isValidTarget } from './config.mjs';
import { logFile } from './process-manager.mjs';
import {
  startTarget,
  stopTarget,
  restartTarget,
  statusRows,
  readLog,
  isPlaywrightInstalled,
  runE2e,
  openTarget,
} from './actions.mjs';
import { startDashboard } from './server.mjs';

function log(message) {
  console.log(`[mission-control] ${message}`);
}

function usage() {
  console.log(`Usage: mission-control <command> [target] [flags]

Commands:
  ui                                   Open the mission control dashboard in your browser
  start [db|backend|frontend|all]     Start one or all services (default: all)
  stop [db|backend|frontend|all]      Stop one or all services (default: all)
  restart [db|backend|frontend|all]   Restart one or all services (default: all)
  status [-w]                         Show state of every service (add -w to keep refreshing)
  logs <db|backend|frontend> [-f]     Show a service's log (dev process output)
  e2e                                 Start every service if needed, then run Playwright
  open [frontend|backend]             Open the running app in your default browser

Flags:
  --open          With start/restart: open the frontend in your browser once healthy
  -f, --follow    With logs: keep streaming new output
  -w, --watch     With status: refresh the table every 2s until Ctrl+C
  -p, --port      With ui: port to serve the dashboard on (default 4400)
`);
}

function printStatusTable() {
  const rows = statusRows();
  const header = ['SERVICE', 'STATE', 'PID', 'PORT'];
  const cells = rows.map((r) => [r.service, r.state, r.pid ? String(r.pid) : '-', String(r.port)]);
  const widths = header.map((h, i) => Math.max(h.length, ...cells.map((r) => r[i].length)));
  const printRow = (r) => console.log(r.map((c, i) => c.padEnd(widths[i])).join('  '));
  printRow(header);
  cells.forEach(printRow);
}

function cmdStatus(watch) {
  if (!watch) {
    printStatusTable();
    return;
  }

  const tick = () => {
    console.clear();
    console.log(`[mission-control] status (refreshing every 2s, Ctrl+C to stop) - ${new Date().toLocaleTimeString()}\n`);
    printStatusTable();
  };

  tick();
  const interval = setInterval(tick, 2000);
  process.on('SIGINT', () => {
    clearInterval(interval);
    process.exit(0);
  });
}

function cmdLogs(target, follow) {
  if (!target || target === 'all') {
    console.log('Specify a service: mission-control logs <db|backend|frontend>');
    return;
  }
  if (target === 'db') {
    spawnSync('docker', ['compose', 'logs', follow ? '-f' : '', '--tail', '200', 'postgres'].filter(Boolean), {
      cwd: ROOT_DIR,
      stdio: 'inherit',
    });
    return;
  }

  const content = readLog(target);
  if (content === null) {
    console.log(`No log file yet for ${target}. Start it first with \`mission-control start ${target}\`.`);
    return;
  }

  process.stdout.write(content);
  if (!follow) return;

  const file = logFile(target);
  let position = fs.statSync(file).size;
  fs.watchFile(file, { interval: 500 }, () => {
    const size = fs.statSync(file).size;
    if (size < position) position = 0;
    const stream = fs.createReadStream(file, { start: position, end: size });
    stream.on('data', (chunk) => process.stdout.write(chunk));
    position = size;
  });
}

async function cmdE2e() {
  if (!isPlaywrightInstalled()) {
    console.log(
      'Playwright is not installed yet. Run:\n  npm install --prefix frontend\n  npx --prefix frontend playwright install --with-deps chromium',
    );
    process.exitCode = 1;
    return;
  }

  await startTarget('all', { open: false, onMessage: log });

  log('running Playwright E2E tests...');
  const exitCode = await runE2e({ onData: (chunk) => process.stdout.write(chunk) });
  process.exitCode = exitCode;
}

function cmdOpen(target) {
  const name = openTarget(target);
  log(`opening ${name === 'backend' ? 'backend' : FRONTEND_URL}`);
}

async function main() {
  const [, , command, ...rest] = process.argv;
  const flags = new Set(rest.filter((a) => a.startsWith('-')));
  const positional = rest.filter((a) => !a.startsWith('-'));
  const target = positional[0] ?? 'all';
  const open = flags.has('--open');
  const follow = flags.has('-f') || flags.has('--follow');
  const watch = flags.has('-w') || flags.has('--watch');
  const portFlagIndex = rest.findIndex((a) => a === '-p' || a === '--port');
  const port = portFlagIndex >= 0 ? Number(rest[portFlagIndex + 1]) : undefined;

  if (!command || command === 'help' || command === '--help' || command === '-h') {
    usage();
    return;
  }

  if (['start', 'stop', 'restart'].includes(command) && !isValidTarget(target)) {
    console.error(`Unknown target "${target}". Use db, backend, frontend, or all.`);
    process.exitCode = 1;
    return;
  }

  switch (command) {
    case 'ui':
      await startDashboard({ port, open: true });
      break;
    case 'start':
      await startTarget(target, { open, onMessage: log });
      break;
    case 'stop':
      stopTarget(target, { onMessage: log });
      break;
    case 'restart':
      await restartTarget(target, { open, onMessage: log });
      break;
    case 'status':
      cmdStatus(watch);
      break;
    case 'logs':
      cmdLogs(positional[0], follow);
      break;
    case 'e2e':
      await cmdE2e();
      break;
    case 'open':
      cmdOpen(positional[0]);
      break;
    default:
      console.error(`Unknown command "${command}".\n`);
      usage();
      process.exitCode = 1;
  }
}

main();
