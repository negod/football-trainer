import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { PeriodList } from './PeriodList';
import type { Period } from '../api/periodsApi';

const period: Period = {
  id: 'p1',
  teamId: 't1',
  name: 'Spring term',
  startDate: '2026-01-01',
  endDate: '2026-06-01',
  format: 'SEVEN_V_SEVEN',
};

describe('PeriodList', () => {
  it('shows an empty state when there are no periods', () => {
    render(<PeriodList periods={[]} onEdit={vi.fn()} />);

    expect(screen.getByText(/no periods yet/i)).toBeInTheDocument();
  });

  it('renders each period including its date range and format', () => {
    render(<PeriodList periods={[period]} onEdit={vi.fn()} />);

    expect(screen.getByText('Spring term')).toBeInTheDocument();
    expect(screen.getByText('2026-01-01 – 2026-06-01 · 7v7')).toBeInTheDocument();
  });

  it('calls onEdit when the edit button is clicked', async () => {
    const user = userEvent.setup();
    const onEdit = vi.fn();

    render(<PeriodList periods={[period]} onEdit={onEdit} />);
    await user.click(screen.getByRole('button', { name: 'Edit' }));

    expect(onEdit).toHaveBeenCalledWith(period);
  });
});
