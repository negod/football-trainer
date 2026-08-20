import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';

import { TeamList } from './TeamList';
import type { Team } from '../api/teamsApi';

const team: Team = { id: '1', name: 'IFK Testby', birthYear: 2019, genderCategory: 'BOYS', shorthand: 'P19' };

describe('TeamList', () => {
  it('shows an empty state when there are no teams', () => {
    render(<TeamList teams={[]} onEdit={vi.fn()} />, { wrapper: MemoryRouter });

    expect(screen.getByText(/no teams yet/i)).toBeInTheDocument();
  });

  it('renders each team and calls onEdit when its edit button is clicked', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(<TeamList teams={[team]} onEdit={onEdit} />, { wrapper: MemoryRouter });

    expect(screen.getByText('IFK Testby')).toBeInTheDocument();
    expect(screen.getByText('P19')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Edit' }));
    expect(onEdit).toHaveBeenCalledWith(team);
  });

  it('links to the team roster', () => {
    render(<TeamList teams={[team]} onEdit={vi.fn()} />, { wrapper: MemoryRouter });

    expect(screen.getByRole('link', { name: 'Roster' })).toHaveAttribute('href', '/teams/1/players');
  });
});
