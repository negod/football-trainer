import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { SessionsPage } from './SessionsPage';
import * as sessionsApi from '../features/sessions/api/sessionsApi';
import type { Session } from '../features/sessions/api/sessionsApi';
import * as periodsApi from '../features/periods/api/periodsApi';
import type { Period } from '../features/periods/api/periodsApi';

vi.mock('../features/sessions/api/sessionsApi');
vi.mock('../features/periods/api/periodsApi');

const period: Period = {
  id: 'p1',
  teamId: '1',
  name: 'Spring term',
  startDate: '2026-01-01',
  endDate: '2026-01-31',
  format: 'SEVEN_V_SEVEN',
};
const session: Session = { id: 's1', periodId: 'p1', date: '2026-01-06', status: 'SCHEDULED' };

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/teams/1/periods/p1/sessions']}>
      <Routes>
        <Route path="/teams/:teamId/periods/:periodId/sessions" element={<SessionsPage />} />
      </Routes>
    </MemoryRouter>,
  );
}

describe('SessionsPage', () => {
  beforeEach(() => {
    vi.resetAllMocks();
    vi.mocked(periodsApi.getPeriod).mockResolvedValue(period);
  });

  it('shows a loading state and then the fetched sessions', async () => {
    vi.mocked(sessionsApi.listSessions).mockResolvedValue([session]);

    renderPage();

    expect(screen.getAllByText(/loading/i).length).toBeGreaterThan(0);
    expect(await screen.findByText('2026-01-06')).toBeInTheDocument();
    expect(await screen.findByRole('heading', { name: 'Spring term sessions' })).toBeInTheDocument();
  });

  it('shows an empty state when there are no sessions yet', async () => {
    vi.mocked(sessionsApi.listSessions).mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText(/no sessions yet/i)).toBeInTheDocument();
  });

  it('shows an error message when loading sessions fails', async () => {
    vi.mocked(sessionsApi.listSessions).mockRejectedValue(new Error('Request failed'));

    renderPage();

    expect(await screen.findByRole('alert')).toHaveTextContent('Request failed');
  });

  it('generates sessions and reloads the list', async () => {
    const user = userEvent.setup();
    vi.mocked(sessionsApi.listSessions).mockResolvedValueOnce([]).mockResolvedValueOnce([session]);
    vi.mocked(sessionsApi.generateSessions).mockResolvedValue([session]);

    renderPage();
    await screen.findByText(/no sessions yet/i);

    await user.click(screen.getByLabelText('Tue'));
    await user.click(screen.getByRole('button', { name: 'Generate sessions' }));

    await waitFor(() => expect(sessionsApi.generateSessions).toHaveBeenCalledWith('1', 'p1', ['TUESDAY']));
    expect(await screen.findByText('2026-01-06')).toBeInTheDocument();
  });
});
