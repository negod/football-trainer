import { spawn } from 'node:child_process';

export function openBrowser(url) {
  const platform = process.platform;

  if (platform === 'win32') {
    spawn('cmd', ['/c', 'start', '""', url], { stdio: 'ignore', detached: true, shell: false }).unref();
    return;
  }

  if (platform === 'darwin') {
    spawn('open', [url], { stdio: 'ignore', detached: true }).unref();
    return;
  }

  spawn('xdg-open', [url], { stdio: 'ignore', detached: true }).unref();
}
