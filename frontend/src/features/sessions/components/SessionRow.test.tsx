import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { SessionRow } from './SessionRow';
import type { Session } from '../api/sessionsApi';

const scheduled: Session = { id: 's1', periodId: 'p1', date: '2026-01-06', status: 'SCHEDULED', source: 'GENERATED' };
const skipped: Session = { ...scheduled, status: 'SKIPPED' };
const adhoc: Session = { ...scheduled, source: 'ADHOC' };

describe('SessionRow', () => {
  it('requires confirmation before skipping', async () => {
    const user = userEvent.setup();
    const onSkip = vi.fn().mockResolvedValue(undefined);

    render(<SessionRow session={scheduled} onSkip={onSkip} onRestore={vi.fn()} onReschedule={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: 'Skip' }));

    expect(onSkip).not.toHaveBeenCalled();
    expect(screen.getByText('Skip this session?')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Confirm skip' }));
    expect(onSkip).toHaveBeenCalledWith(scheduled);
  });

  it('cancels a pending skip confirmation without calling onSkip', async () => {
    const user = userEvent.setup();
    const onSkip = vi.fn();

    render(<SessionRow session={scheduled} onSkip={onSkip} onRestore={vi.fn()} onReschedule={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: 'Skip' }));
    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    expect(onSkip).not.toHaveBeenCalled();
    expect(screen.queryByText('Skip this session?')).not.toBeInTheDocument();
  });

  it('shows a Skipped label and a Restore action for a skipped session, restoring without confirmation', async () => {
    const user = userEvent.setup();
    const onRestore = vi.fn().mockResolvedValue(undefined);

    render(<SessionRow session={skipped} onSkip={vi.fn()} onRestore={onRestore} onReschedule={vi.fn()} />);

    expect(screen.getByText('Skipped')).toBeInTheDocument();
    await user.click(screen.getByRole('button', { name: 'Restore' }));

    expect(onRestore).toHaveBeenCalledWith(skipped);
  });

  it('reschedules to a new date', async () => {
    const user = userEvent.setup();
    const onReschedule = vi.fn().mockResolvedValue(undefined);

    render(<SessionRow session={scheduled} onSkip={vi.fn()} onRestore={vi.fn()} onReschedule={onReschedule} />);
    await user.click(screen.getByRole('button', { name: 'Reschedule' }));
    const dateInput = screen.getByLabelText('New date');
    await user.clear(dateInput);
    await user.type(dateInput, '2026-01-13');
    await user.click(screen.getByRole('button', { name: 'Save' }));

    expect(onReschedule).toHaveBeenCalledWith(scheduled, '2026-01-13');
  });

  it('shows an Ad-hoc badge for ad-hoc sessions', () => {
    render(<SessionRow session={adhoc} onSkip={vi.fn()} onRestore={vi.fn()} onReschedule={vi.fn()} />);

    expect(screen.getByText('Ad-hoc')).toBeInTheDocument();
  });

  it('shows an error message when an action fails', async () => {
    const user = userEvent.setup();
    const onSkip = vi.fn().mockRejectedValue(new Error('Could not skip'));

    render(<SessionRow session={scheduled} onSkip={onSkip} onRestore={vi.fn()} onReschedule={vi.fn()} />);
    await user.click(screen.getByRole('button', { name: 'Skip' }));
    await user.click(screen.getByRole('button', { name: 'Confirm skip' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Could not skip');
  });
});
