import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';

import { ErrorMessage } from './ErrorMessage';

describe('ErrorMessage', () => {
  it('renders the message with an alert role', () => {
    render(<ErrorMessage message="Something went wrong" />);

    expect(screen.getByRole('alert')).toHaveTextContent('Something went wrong');
  });
});
