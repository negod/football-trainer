import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { PlayerList } from './PlayerList';
import type { Player } from '../api/playersApi';

const player: Player = { id: 'p1', teamId: 't1', name: 'Alex Andersson', birthYear: 2015, position: 'Forward' };

describe('PlayerList', () => {
  it('shows an empty state when there are no players', () => {
    render(<PlayerList players={[]} onEdit={vi.fn()} onRemove={vi.fn()} />);

    expect(screen.getByText(/no players yet/i)).toBeInTheDocument();
  });

  it('renders each player including position', () => {
    render(<PlayerList players={[player]} onEdit={vi.fn()} onRemove={vi.fn()} />);

    expect(screen.getByText('Alex Andersson')).toBeInTheDocument();
    expect(screen.getByText('2015 · Forward')).toBeInTheDocument();
  });

  it('renders a player without a position', () => {
    render(<PlayerList players={[{ ...player, position: null }]} onEdit={vi.fn()} onRemove={vi.fn()} />);

    expect(screen.getByText('2015')).toBeInTheDocument();
  });

  it('calls onEdit when the edit button is clicked', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(<PlayerList players={[player]} onEdit={onEdit} onRemove={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: 'Edit' }));

    expect(onEdit).toHaveBeenCalledWith(player);
  });

  it('calls onRemove when the remove button is clicked', async () => {
    const user = userEvent.setup();
    const onRemove = vi.fn();

    render(<PlayerList players={[player]} onEdit={vi.fn()} onRemove={onRemove} />);
    await user.click(screen.getByRole('button', { name: 'Remove' }));

    expect(onRemove).toHaveBeenCalledWith(player);
  });
});
