import { useState, type FormEvent } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { PlayerInput } from '../api/playersApi';

type PlayerFormProps = {
  initialValue?: PlayerInput;
  submitLabel: string;
  onSubmit: (input: PlayerInput) => Promise<void>;
  onCancel?: () => void;
};

const emptyValue: PlayerInput = { name: '', birthYear: new Date().getFullYear(), position: '' };

export function PlayerForm({ initialValue, submitLabel, onSubmit, onCancel }: PlayerFormProps) {
  const [name, setName] = useState(initialValue?.name ?? emptyValue.name);
  const [birthYear, setBirthYear] = useState(initialValue?.birthYear ?? emptyValue.birthYear);
  const [position, setPosition] = useState(initialValue?.position ?? '');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit({ name, birthYear, position: position.trim() ? position.trim() : null });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Could not save the player');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
      {error && <ErrorMessage message={error} />}

      <div className="flex flex-col gap-1">
        <label htmlFor="player-name" className="text-sm font-medium text-slate-800">
          Player name
        </label>
        <input
          id="player-name"
          name="name"
          type="text"
          required
          maxLength={100}
          value={name}
          onChange={(event) => setName(event.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="player-birth-year" className="text-sm font-medium text-slate-800">
          Birth year
        </label>
        <input
          id="player-birth-year"
          name="birthYear"
          type="number"
          required
          value={birthYear}
          onChange={(event) => setBirthYear(Number(event.target.value))}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="player-position" className="text-sm font-medium text-slate-800">
          Position (optional)
        </label>
        <input
          id="player-position"
          name="position"
          type="text"
          maxLength={50}
          value={position}
          onChange={(event) => setPosition(event.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={submitting}
          className="rounded-md bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800 disabled:opacity-60"
        >
          {submitting ? 'Saving...' : submitLabel}
        </button>
        {onCancel && (
          <button
            type="button"
            onClick={onCancel}
            className="rounded-md border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  );
}
