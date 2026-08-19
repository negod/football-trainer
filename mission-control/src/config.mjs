import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export const ROOT_DIR = path.resolve(__dirname, '..', '..');
export const STATE_DIR = path.join(__dirname, '..', '.state');
export const PID_DIR = path.join(STATE_DIR, 'pids');
export const LOG_DIR = path.join(STATE_DIR, 'logs');

export const BACKEND_URL = 'http://localhost:8080';
export const FRONTEND_URL = 'http://localhost:5173';
export const DASHBOARD_PORT = 4400;

export const SERVICE_ORDER = ['db', 'backend', 'frontend'];

export const PROCESS_SERVICES = {
  backend: {
    command: 'npm run dev:backend',
    cwd: ROOT_DIR,
    healthUrl: `${BACKEND_URL}/api/health`,
    readyMessage: `backend healthy at ${BACKEND_URL}`,
  },
  frontend: {
    command: 'npm run dev:frontend',
    cwd: ROOT_DIR,
    healthUrl: FRONTEND_URL,
    readyMessage: `frontend healthy at ${FRONTEND_URL}`,
  },
};

export function isValidTarget(target) {
  return target === 'all' || target === 'db' || target === 'backend' || target === 'frontend';
}
