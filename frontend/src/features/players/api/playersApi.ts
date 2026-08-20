import { apiRequest } from '../../../shared/api/apiClient';

export type Player = {
  id: string;
  teamId: string;
  name: string;
  birthYear: number;
  position: string | null;
};

export type PlayerInput = {
  name: string;
  birthYear: number;
  position: string | null;
};

export function listPlayers(teamId: string): Promise<Player[]> {
  return apiRequest<Player[]>(`/teams/${teamId}/players`);
}

export function createPlayer(teamId: string, input: PlayerInput): Promise<Player> {
  return apiRequest<Player>(`/teams/${teamId}/players`, {
    method: 'POST',
    body: JSON.stringify(input),
  });
}

export function updatePlayer(teamId: string, id: string, input: PlayerInput): Promise<Player> {
  return apiRequest<Player>(`/teams/${teamId}/players/${id}`, {
    method: 'PATCH',
    body: JSON.stringify(input),
  });
}

export function deletePlayer(teamId: string, id: string): Promise<void> {
  return apiRequest<void>(`/teams/${teamId}/players/${id}`, {
    method: 'DELETE',
  });
}
