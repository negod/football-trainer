import { spawnSync } from 'node:child_process';
import { ROOT_DIR } from './config.mjs';

function compose(args, { capture = false } = {}) {
  return spawnSync('docker', ['compose', ...args], {
    cwd: ROOT_DIR,
    stdio: capture ? 'pipe' : 'inherit',
    encoding: 'utf8',
  });
}

export function startDb() {
  return compose(['up', '-d', 'postgres']);
}

export function stopDb() {
  return compose(['stop', 'postgres']);
}

export function restartDb() {
  return compose(['restart', 'postgres']);
}

function containerId() {
  const result = compose(['ps', '-q', 'postgres'], { capture: true });
  const id = result.stdout?.trim();
  return id || null;
}

export function dbHealth() {
  const id = containerId();
  if (!id) return 'stopped';

  const inspect = spawnSync('docker', ['inspect', '-f', '{{.State.Health.Status}}', id], {
    encoding: 'utf8',
  });
  const health = inspect.stdout?.trim();
  return health || 'starting';
}

export function isDbRunning() {
  return containerId() !== null;
}

export function dbLogsText({ tail = 200 } = {}) {
  const result = compose(['logs', '--no-color', '--tail', String(tail), 'postgres'], { capture: true });
  return (result.stdout ?? '') + (result.stderr ?? '');
}
