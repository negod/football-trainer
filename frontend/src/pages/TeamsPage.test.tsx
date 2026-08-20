import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { TeamsPage } from './TeamsPage';
import * as teamsApi from '../features/teams/api/teamsApi';
import type { Team } from '../features/teams/api/teamsApi';

vi.mock('../features/teams/api/teamsApi');

const team: Team = { id: '1', name: 'IFK Testby', birthYear: 2019, genderCategory: 'BOYS', shorthand: 'P19' };

describe('TeamsPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  it('shows a loading state and then the fetched teams', async () => {
    vi.mocked(teamsApi.listTeams).mockResolvedValue([team]);

    render(<TeamsPage />);

    expect(screen.getByText(/loading/i)).toBeInTheDocument();
    expect(await screen.findByText('IFK Testby')).toBeInTheDocument();
  });

  it('shows an empty state when there are no teams yet', async () => {
    vi.mocked(teamsApi.listTeams).mockResolvedValue([]);

    render(<TeamsPage />);

    expect(await screen.findByText(/no teams yet/i)).toBeInTheDocument();
  });

  it('shows an error message when loading fails', async () => {
    vi.mocked(teamsApi.listTeams).mockRejectedValue(new Error('Request failed'));

    render(<TeamsPage />);

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed');
  });

  it('creates a team and reloads the list', async () => {
    const user = userEvent.setup();
    vi.mocked(teamsApi.listTeams).mockResolvedValueOnce([]).mockResolvedValueOnce([team]);
    vi.mocked(teamsApi.createTeam).mockResolvedValue(team);

    render(<TeamsPage />);
    await screen.findByText(/no teams yet/i);

    await user.type(screen.getByLabelText('Team name'), 'IFK Testby');
    await user.clear(screen.getByLabelText('Birth year'));
    await user.type(screen.getByLabelText('Birth year'), '2019');
    await user.click(screen.getByRole('button', { name: 'Create team' }));

    await waitFor(() => expect(teamsApi.createTeam).toHaveBeenCalledWith({
      name: 'IFK Testby',
      birthYear: 2019,
      genderCategory: 'BOYS',
    }));
    expect(await screen.findByText('IFK Testby')).toBeInTheDocument();
  });

  it('switches to edit mode and saves changes', async () => {
    const user = userEvent.setup();
    vi.mocked(teamsApi.listTeams).mockResolvedValue([team]);
    vi.mocked(teamsApi.updateTeam).mockResolvedValue({ ...team, name: 'Renamed' });

    render(<TeamsPage />);
    await user.click(await screen.findByRole('button', { name: 'Edit' }));

    expect(screen.getByRole('heading', { name: 'Edit IFK Testby' })).toBeInTheDocument();
    expect(screen.getByLabelText('Team name')).toHaveValue('IFK Testby');

    await user.clear(screen.getByLabelText('Team name'));
    await user.type(screen.getByLabelText('Team name'), 'Renamed');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));

    await waitFor(() => expect(teamsApi.updateTeam).toHaveBeenCalledWith('1', {
      name: 'Renamed',
      birthYear: 2019,
      genderCategory: 'BOYS',
    }));
  });
});
