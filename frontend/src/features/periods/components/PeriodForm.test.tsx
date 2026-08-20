import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { PeriodForm } from './PeriodForm';

describe('PeriodForm', () => {
  it('submits the entered values', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<PeriodForm submitLabel="Create period" onSubmit={onSubmit} />);

    await user.clear(screen.getByLabelText('Period name'));
    await user.type(screen.getByLabelText('Period name'), 'Spring term');
    await user.clear(screen.getByLabelText('Start date'));
    await user.type(screen.getByLabelText('Start date'), '2026-01-01');
    await user.clear(screen.getByLabelText('End date'));
    await user.type(screen.getByLabelText('End date'), '2026-06-01');
    await user.selectOptions(screen.getByLabelText(/format/i), '5v5');
    await user.click(screen.getByRole('button', { name: 'Create period' }));

    expect(onSubmit).toHaveBeenCalledWith({
      name: 'Spring term',
      startDate: '2026-01-01',
      endDate: '2026-06-01',
      format: 'FIVE_V_FIVE',
    });
  });

  it('defaults the format to the suggestion when creating', () => {
    render(<PeriodForm submitLabel="Create period" suggestedFormat="NINE_V_NINE" onSubmit={vi.fn()} />);

    expect(screen.getByLabelText(/format/i)).toHaveValue('NINE_V_NINE');
  });

  it('shows an error message when submission fails', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Period end date must be after the start date'));

    render(<PeriodForm submitLabel="Create period" onSubmit={onSubmit} />);
    await user.click(screen.getByRole('button', { name: 'Create period' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Period end date must be after the start date');
  });

  it('pre-fills fields from the initial value and calls onCancel, ignoring any suggestion', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(
      <PeriodForm
        initialValue={{ name: 'Existing period', startDate: '2025-01-01', endDate: '2025-06-01', format: 'ELEVEN_V_ELEVEN' }}
        suggestedFormat="THREE_V_THREE"
        submitLabel="Save changes"
        onSubmit={vi.fn().mockResolvedValue(undefined)}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByLabelText('Period name')).toHaveValue('Existing period');
    expect(screen.getByLabelText('Start date')).toHaveValue('2025-01-01');
    expect(screen.getByLabelText('End date')).toHaveValue('2025-06-01');
    expect(screen.getByLabelText(/format/i)).toHaveValue('ELEVEN_V_ELEVEN');

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
