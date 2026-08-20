import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';

import { TeamForm } from './TeamForm';

describe('TeamForm', () => {
  it('submits the entered values', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue(undefined);

    render(<TeamForm submitLabel="Create team" onSubmit={onSubmit} />);

    await user.clear(screen.getByLabelText('Team name'));
    await user.type(screen.getByLabelText('Team name'), 'IFK Testby P19');
    await user.clear(screen.getByLabelText('Birth year'));
    await user.type(screen.getByLabelText('Birth year'), '2019');
    await user.selectOptions(screen.getByLabelText('Gender category'), 'GIRLS');
    await user.click(screen.getByRole('button', { name: 'Create team' }));

    expect(onSubmit).toHaveBeenCalledWith({ name: 'IFK Testby P19', birthYear: 2019, genderCategory: 'GIRLS' });
  });

  it('shows an error message when submission fails', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockRejectedValue(new Error('Team name must not be blank'));

    render(<TeamForm submitLabel="Create team" onSubmit={onSubmit} />);
    await user.click(screen.getByRole('button', { name: 'Create team' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Team name must not be blank');
  });

  it('pre-fills fields from the initial value and calls onCancel', async () => {
    const user = userEvent.setup();
    const onCancel = vi.fn();

    render(
      <TeamForm
        initialValue={{ name: 'Existing team', birthYear: 2018, genderCategory: 'MIXED' }}
        submitLabel="Save changes"
        onSubmit={vi.fn().mockResolvedValue(undefined)}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByLabelText('Team name')).toHaveValue('Existing team');
    expect(screen.getByLabelText('Birth year')).toHaveValue(2018);
    expect(screen.getByLabelText('Gender category')).toHaveValue('MIXED');

    await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(onCancel).toHaveBeenCalled();
  });
});
