import { apiRequest } from '../../../shared/api/apiClient';

export type Weekday = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export type SessionStatus = 'SCHEDULED' | 'SKIPPED';

export type SessionSource = 'GENERATED' | 'ADHOC';

export type Session = {
  id: string;
  periodId: string;
  date: string;
  status: SessionStatus;
  source: SessionSource;
};

export type UpdateSessionInput = {
  status?: SessionStatus;
  date?: string;
};

export function listSessions(teamId: string, periodId: string): Promise<Session[]> {
  return apiRequest<Session[]>(`/teams/${teamId}/periods/${periodId}/sessions`);
}

export function generateSessions(teamId: string, periodId: string, weekdays: Weekday[]): Promise<Session[]> {
  return apiRequest<Session[]>(`/teams/${teamId}/periods/${periodId}/generate-sessions`, {
    method: 'POST',
    body: JSON.stringify({ weekdays }),
  });
}

export function addAdhocSession(teamId: string, periodId: string, date: string): Promise<Session> {
  return apiRequest<Session>(`/teams/${teamId}/periods/${periodId}/sessions`, {
    method: 'POST',
    body: JSON.stringify({ date }),
  });
}

export function updateSession(teamId: string, periodId: string, sessionId: string, input: UpdateSessionInput): Promise<Session> {
  return apiRequest<Session>(`/teams/${teamId}/periods/${periodId}/sessions/${sessionId}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  });
}
