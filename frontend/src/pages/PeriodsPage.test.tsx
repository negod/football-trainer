import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { PeriodsPage } from './PeriodsPage';
import * as periodsApi from '../features/periods/api/periodsApi';
import type { Period } from '../features/periods/api/periodsApi';
import * as teamsApi from '../features/teams/api/teamsApi';
import type { Team } from '../features/teams/api/teamsApi';

vi.mock('../features/periods/api/periodsApi');
vi.mock('../features/teams/api/teamsApi');

const team: Team = { id: '1', name: 'IFK Testby', birthYear: 2015, genderCategory: 'BOYS', shorthand: 'P15' };
const period: Period = {
  id: 'p1',
  teamId: '1',
  name: 'Spring term',
  startDate: '2026-01-01',
  endDate: '2026-06-01',
  format: 'SEVEN_V_SEVEN',
};

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/teams/1/periods']}>
      <Routes>
        <Route path="/teams/:teamId/periods" element={<PeriodsPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('PeriodsPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(teamsApi.getTeam).mockResolvedValue(team);
    vi.mocked(periodsApi.getSuggestedFormat).mockResolvedValue('SEVEN_V_SEVEN');
  });

  it('shows a loading state and then the fetched periods', async () => {
    vi.mocked(periodsApi.listPeriods).mockResolvedValue([period]);

    renderPage();

    expect(screen.getAllByText(/loading/i).length).toBeGreaterThan(0);
    expect(await screen.findByText('Spring term')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'IFK Testby periods' })).toBeInTheDocument();
  });

  it('shows an empty state when there are no periods yet', async () => {
    vi.mocked(periodsApi.listPeriods).mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText(/no periods yet/i)).toBeInTheDocument();
  });

  it('shows an error message when loading periods fails', async () => {
    vi.mocked(periodsApi.listPeriods).mockRejectedValue(new Error('Request failed'));

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed');
  });

  it('pre-fills the create form with the suggested format', async () => {
    vi.mocked(periodsApi.listPeriods).mockResolvedValue([]);
    vi.mocked(periodsApi.getSuggestedFormat).mockResolvedValue('NINE_V_NINE');

    renderPage();

    await waitFor(() => expect(screen.getByLabelText(/format/i)).toHaveValue('NINE_V_NINE'));
  });

  it('creates a period and reloads the list', async () => {
    const user = userEvent.setup();
    vi.mocked(periodsApi.listPeriods).mockResolvedValueOnce([]).mockResolvedValueOnce([period]);
    vi.mocked(periodsApi.createPeriod).mockResolvedValue(period);

    renderPage();
    await screen.findByText(/no periods yet/i);

    await user.type(screen.getByLabelText('Period name'), 'Spring term');
    await user.clear(screen.getByLabelText('Start date'));
    await user.type(screen.getByLabelText('Start date'), '2026-01-01');
    await user.clear(screen.getByLabelText('End date'));
    await user.type(screen.getByLabelText('End date'), '2026-06-01');
    await user.click(screen.getByRole('button', { name: 'Create period' }));

    await waitFor(() => expect(periodsApi.createPeriod).toHaveBeenCalledWith('1', {
      name: 'Spring term',
      startDate: '2026-01-01',
      endDate: '2026-06-01',
      format: 'SEVEN_V_SEVEN',
    }));
    expect(await screen.findByText('Spring term')).toBeInTheDocument();
  });

  it('switches to edit mode and saves changes', async () => {
    const user = userEvent.setup();
    vi.mocked(periodsApi.listPeriods).mockResolvedValue([period]);
    vi.mocked(periodsApi.updatePeriod).mockResolvedValue({ ...period, name: 'Renamed' });

    renderPage();
    await user.click(await screen.findByRole('button', { name: 'Edit' }));

    expect(screen.getByRole('heading', { name: 'Edit Spring term' })).toBeInTheDocument();
    expect(screen.getByLabelText('Period name')).toHaveValue('Spring term');

    await user.clear(screen.getByLabelText('Period name'));
    await user.type(screen.getByLabelText('Period name'), 'Renamed');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));

    await waitFor(() => expect(periodsApi.updatePeriod).toHaveBeenCalledWith('1', 'p1', {
      name: 'Renamed',
      startDate: '2026-01-01',
      endDate: '2026-06-01',
      format: 'SEVEN_V_SEVEN',
    }));
  });
});
