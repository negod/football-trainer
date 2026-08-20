import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { SessionGenerationForm } from './SessionGenerationForm';

describe('SessionGenerationForm', () => {
  it('disables submission until a weekday is selected and shows a preview count once one is', async () => {
    const user = userEvent.setup();

    render(
      <SessionGenerationForm periodStartDate="2026-01-01" periodEndDate="2026-01-31" onGenerate={vi.fn()} />,
    );

    expect(screen.getByText(/select at least one practice day/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate sessions' })).toBeDisabled();

    await user.click(screen.getByLabelText('Tue'));

    expect(screen.getByText(/this will generate 4 sessions/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Generate sessions' })).toBeEnabled();
  });

  it('submits the selected weekdays', async () => {
    const user = userEvent.setup();
    const onGenerate = vi.fn().mockResolvedValue(undefined);

    render(
      <SessionGenerationForm periodStartDate="2026-01-01" periodEndDate="2026-01-31" onGenerate={onGenerate} />,
    );

    await user.click(screen.getByLabelText('Tue'));
    await user.click(screen.getByLabelText('Thu'));
    await user.click(screen.getByRole('button', { name: 'Generate sessions' }));

    expect(onGenerate).toHaveBeenCalledWith(['TUESDAY', 'THURSDAY']);
  });

  it('shows an error message when generation fails', async () => {
    const user = userEvent.setup();
    const onGenerate = vi.fn().mockRejectedValue(new Error('At least one weekday must be selected'));

    render(
      <SessionGenerationForm periodStartDate="2026-01-01" periodEndDate="2026-01-31" onGenerate={onGenerate} />,
    );

    await user.click(screen.getByLabelText('Mon'));
    await user.click(screen.getByRole('button', { name: 'Generate sessions' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('At least one weekday must be selected');
  });
});
