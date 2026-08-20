import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { PlayerForm } from './PlayerForm';

describe('PlayerForm', () => {
  it('submits the entered values', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<PlayerForm submitLabel="Add player" onSubmit={onSubmit} />);

    await user.clear(screen.getByLabelText('Player name'));
    await user.type(screen.getByLabelText('Player name'), 'Alex Andersson');
    await user.clear(screen.getByLabelText('Birth year'));
    await user.type(screen.getByLabelText('Birth year'), '2015');
    await user.type(screen.getByLabelText('Position (optional)'), 'Forward');
    await user.click(screen.getByRole('button', { name: 'Add player' }));

    expect(onSubmit).toHaveBeenCalledWith({ name: 'Alex Andersson', birthYear: 2015, position: 'Forward' });
  });

  it('submits a null position when left blank', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<PlayerForm submitLabel="Add player" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText('Player name'), 'Alex Andersson');
    await user.click(screen.getByRole('button', { name: 'Add player' }));

    expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({ position: null }));
  });

  it('shows an error message when submission fails', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Player name must not be blank'));

    render(<PlayerForm submitLabel="Add player" onSubmit={onSubmit} />);
    await user.click(screen.getByRole('button', { name: 'Add player' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Player name must not be blank');
  });

  it('pre-fills fields from the initial value and calls onCancel', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(
      <PlayerForm
        initialValue={{ name: 'Existing player', birthYear: 2014, position: 'Defender' }}
        submitLabel="Save changes"
        onSubmit={vi.fn().mockResolvedValue(undefined)}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByLabelText('Player name')).toHaveValue('Existing player');
    expect(screen.getByLabelText('Birth year')).toHaveValue(2014);
    expect(screen.getByLabelText('Position (optional)')).toHaveValue('Defender');

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
