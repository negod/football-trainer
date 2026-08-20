import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';

import { SessionList } from './SessionList';
import type { Session } from '../api/sessionsApi';

const session: Session = { id: 's1', periodId: 'p1', date: '2026-01-06', status: 'SCHEDULED', source: 'GENERATED' };

describe('SessionList', () => {
  it('shows an empty state when there are no sessions', () => {
    render(<SessionList sessions={[]} onSkip={vi.fn()} onRestore={vi.fn()} onReschedule={vi.fn()} />);

    expect(screen.getByText(/no sessions yet/i)).toBeInTheDocument();
  });

  it('renders each session date', () => {
    render(<SessionList sessions={[session]} onSkip={vi.fn()} onRestore={vi.fn()} onReschedule={vi.fn()} />);

    expect(screen.getByText('2026-01-06')).toBeInTheDocument();
  });
});
