export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly details: string[] = [],
  ) {
    super(message);
  }
}

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const body = await parseErrorBody(response);
    throw new ApiError(body.message, response.status, body.details);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return text ? (JSON.parse(text) as T) : (undefined as T);
}

async function parseErrorBody(response: Response): Promise<{ message: string; details: string[] }> {
  const fallback = { message: `Request failed with status ${response.status}`, details: [] };
  const text = await response.text();

  if (!text) {
    return fallback;
  }

  try {
    const body = JSON.parse(text) as { message?: unknown; details?: unknown };
    return {
      message: typeof body.message === 'string' && body.message.trim() ? body.message : fallback.message,
      details: Array.isArray(body.details) ? body.details.filter((detail): detail is string => typeof detail === 'string') : [],
    };
  } catch {
    return fallback;
  }
}

