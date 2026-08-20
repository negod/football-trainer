import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { PlayersPage } from './PlayersPage';
import * as playersApi from '../features/players/api/playersApi';
import type { Player } from '../features/players/api/playersApi';
import * as teamsApi from '../features/teams/api/teamsApi';
import type { Team } from '../features/teams/api/teamsApi';

vi.mock('../features/players/api/playersApi');
vi.mock('../features/teams/api/teamsApi');

const team: Team = { id: '1', name: 'IFK Testby', birthYear: 2019, genderCategory: 'BOYS', shorthand: 'P19' };
const player: Player = { id: 'p1', teamId: '1', name: 'Alex Andersson', birthYear: 2015, position: 'Forward' };

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/teams/1/players']}>
      <Routes>
        <Route path="/teams/:teamId/players" element={<PlayersPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('PlayersPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(teamsApi.getTeam).mockResolvedValue(team);
  });

  it('shows a loading state and then the fetched players', async () => {
    vi.mocked(playersApi.listPlayers).mockResolvedValue([player]);

    renderPage();

    expect(screen.getAllByText(/loading/i).length).toBeGreaterThan(0);
    expect(await screen.findByText('Alex Andersson')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'IFK Testby roster' })).toBeInTheDocument();
  });

  it('shows an empty state when there are no players yet', async () => {
    vi.mocked(playersApi.listPlayers).mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText(/no players yet/i)).toBeInTheDocument();
  });

  it('shows an error message when loading the roster fails', async () => {
    vi.mocked(playersApi.listPlayers).mockRejectedValue(new Error('Request failed'));

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed');
  });

  it('adds a player and reloads the roster', async () => {
    const user = userEvent.setup();
    vi.mocked(playersApi.listPlayers).mockResolvedValueOnce([]).mockResolvedValueOnce([player]);
    vi.mocked(playersApi.createPlayer).mockResolvedValue(player);

    renderPage();
    await screen.findByText(/no players yet/i);

    await user.type(screen.getByLabelText('Player name'), 'Alex Andersson');
    await user.clear(screen.getByLabelText('Birth year'));
    await user.type(screen.getByLabelText('Birth year'), '2015');
    await user.type(screen.getByLabelText('Position (optional)'), 'Forward');
    await user.click(screen.getByRole('button', { name: 'Add player' }));

    await waitFor(() => expect(playersApi.createPlayer).toHaveBeenCalledWith('1', {
      name: 'Alex Andersson',
      birthYear: 2015,
      position: 'Forward',
    }));
    expect(await screen.findByText('Alex Andersson')).toBeInTheDocument();
  });

  it('switches to edit mode and saves changes', async () => {
    const user = userEvent.setup();
    vi.mocked(playersApi.listPlayers).mockResolvedValue([player]);
    vi.mocked(playersApi.updatePlayer).mockResolvedValue({ ...player, name: 'Renamed' });

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Edit' }));

    expect(screen.getByRole('heading', { name: 'Edit Alex Andersson' })).toBeInTheDocument();
    expect(screen.getByLabelText('Player name')).toHaveValue('Alex Andersson');

    await user.clear(screen.getByLabelText('Player name'));
    await user.type(screen.getByLabelText('Player name'), 'Renamed');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));

    await waitFor(() => expect(playersApi.updatePlayer).toHaveBeenCalledWith('1', 'p1', {
      name: 'Renamed',
      birthYear: 2015,
      position: 'Forward',
    }));
  });

  it('removes a player', async () => {
    const user = userEvent.setup();
    vi.mocked(playersApi.listPlayers).mockResolvedValueOnce([player]).mockResolvedValueOnce([]);
    vi.mocked(playersApi.deletePlayer).mockResolvedValue(undefined);

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Remove' }));

    await waitFor(() => expect(playersApi.deletePlayer).toHaveBeenCalledWith('1', 'p1'));
    expect(await screen.findByText(/no players yet/i)).toBeInTheDocument();
  });

  it('shows an error message when removing a player fails', async () => {
    const user = userEvent.setup();
    vi.mocked(playersApi.listPlayers).mockResolvedValue([player]);
    vi.mocked(playersApi.deletePlayer).mockRejectedValue(new Error('Could not remove player'));

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Remove' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not remove player');
  });
});
