import { apiRequest } from '../../../shared/api/apiClient';

export type MatchFormat = 'THREE_V_THREE' | 'FIVE_V_FIVE' | 'SEVEN_V_SEVEN' | 'NINE_V_NINE' | 'ELEVEN_V_ELEVEN';

export type Period = {
  id: string;
  teamId: string;
  name: string;
  startDate: string;
  endDate: string;
  format: MatchFormat;
};

export type PeriodInput = {
  name: string;
  startDate: string;
  endDate: string;
  format: MatchFormat;
};

export function listPeriods(teamId: string): Promise<Period[]> {
  return apiRequest<Period[]>(`/teams/${teamId}/periods`);
}

export function getPeriod(teamId: string, id: string): Promise<Period> {
  return apiRequest<Period>(`/teams/${teamId}/periods/${id}`);
}

export function createPeriod(teamId: string, input: PeriodInput): Promise<Period> {
  return apiRequest<Period>(`/teams/${teamId}/periods`, {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updatePeriod(teamId: string, id: string, input: PeriodInput): Promise<Period> {
  return apiRequest<Period>(`/teams/${teamId}/periods/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  });
}

export async function getSuggestedFormat(teamId: string): Promise<MatchFormat> {
  const response = await apiRequest<{ format: MatchFormat }>(`/teams/${teamId}/periods/suggested-format`);
  return response.format;
}
