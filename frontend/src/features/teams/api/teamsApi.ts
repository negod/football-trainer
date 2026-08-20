import { apiRequest } from '../../../shared/api/apiClient';

export type GenderCategory = 'BOYS' | 'GIRLS' | 'MIXED';

export type Team = {
  id: string;
  name: string;
  birthYear: number;
  genderCategory: GenderCategory;
  shorthand: string;
};

export type TeamInput = {
  name: string;
  birthYear: number;
  genderCategory: GenderCategory;
};

export function listTeams(): Promise<Team[]> {
  return apiRequest<Team[]>('/teams');
}

export function createTeam(input: TeamInput): Promise<Team> {
  return apiRequest<Team>('/teams', {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updateTeam(id: string, input: TeamInput): Promise<Team> {
  return apiRequest<Team>(`/teams/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  });
}
