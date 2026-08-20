import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { AddAdhocSessionForm } from './AddAdhocSessionForm';

describe('AddAdhocSessionForm', () => {
  it('submits the selected date', async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn().mockResolvedValue(undefined);

    render(<AddAdhocSessionForm onAdd={onAdd} />);

    const dateInput = screen.getByLabelText('Date');
    await user.clear(dateInput);
    await user.type(dateInput, '2026-01-10');
    await user.click(screen.getByRole('button', { name: 'Add session' }));

    expect(onAdd).toHaveBeenCalledWith('2026-01-10');
  });

  it('shows an error message when adding fails', async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn().mockRejectedValue(new Error('A session already exists on 2026-01-10'));

    render(<AddAdhocSessionForm onAdd={onAdd} />);
    await user.click(screen.getByRole('button', { name: 'Add session' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('A session already exists on 2026-01-10');
  });
});
