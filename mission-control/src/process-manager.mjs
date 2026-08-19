import { spawn } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { PID_DIR, LOG_DIR } from './config.mjs';

const LAUNCHER_PATH = path.join(path.dirname(fileURLToPath(import.meta.url)), 'launcher.cjs');

function ensureStateDirs() {
  fs.mkdirSync(PID_DIR, { recursive: true });
  fs.mkdirSync(LOG_DIR, { recursive: true });
}

function pidFile(name) {
  return path.join(PID_DIR, `${name}.json`);
}

export function logFile(name) {
  return path.join(LOG_DIR, `${name}.log`);
}

function readPidInfo(name) {
  try {
    return JSON.parse(fs.readFileSync(pidFile(name), 'utf8'));
  } catch {
    return null;
  }
}

function isAlive(pid) {
  try {
    process.kill(pid, 0);
    return true;
  } catch {
    return false;
  }
}

export function isRunning(name) {
  const info = readPidInfo(name);
  if (!info) return false;
  if (!isAlive(info.pid)) {
    clearPidInfo(name);
    return false;
  }
  return true;
}

export function getPid(name) {
  const info = readPidInfo(name);
  return info && isAlive(info.pid) ? info.pid : null;
}

function clearPidInfo(name) {
  try {
    fs.unlinkSync(pidFile(name));
  } catch {
    // already gone
  }
}

export function startProcess(name, { command, cwd }) {
  if (isRunning(name)) {
    return { alreadyRunning: true, pid: getPid(name) };
  }

  ensureStateDirs();
  const out = fs.openSync(logFile(name), 'a');
  // Spawn node.exe (running launcher.cjs) as the detached top-level process,
  // which then runs `command` as its own normal child - see launcher.cjs for
  // why this indirection is needed on Windows.
  const child = spawn(process.execPath, [LAUNCHER_PATH, command], {
    cwd,
    detached: true,
    stdio: ['ignore', out, out],
  });
  child.unref();

  fs.writeFileSync(
    pidFile(name),
    JSON.stringify({ pid: child.pid, command, startedAt: new Date().toISOString() }, null, 2),
  );

  return { alreadyRunning: false, pid: child.pid };
}

export function stopProcess(name) {
  const info = readPidInfo(name);
  if (!info || !isAlive(info.pid)) {
    clearPidInfo(name);
    return { wasRunning: false };
  }

  if (process.platform === 'win32') {
    spawn('taskkill', ['/PID', String(info.pid), '/T', '/F'], { stdio: 'ignore' });
  } else {
    try {
      process.kill(-info.pid, 'SIGTERM');
    } catch {
      try {
        process.kill(info.pid, 'SIGTERM');
      } catch {
        // already gone
      }
    }
  }

  clearPidInfo(name);
  return { wasRunning: true };
}
