import { apiRequest } from '../../../shared/api/apiClient';

export type Weekday = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export type SessionStatus = 'SCHEDULED';

export type Session = {
  id: string;
  periodId: string;
  date: string;
  status: SessionStatus;
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
