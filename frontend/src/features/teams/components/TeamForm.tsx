import { useState, type FormEvent } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { GenderCategory, TeamInput } from '../api/teamsApi';

type TeamFormProps = {
  initialValue?: TeamInput;
  submitLabel: string;
  onSubmit: (input: TeamInput) => Promise<void>;
  onCancel?: () => void;
};

const emptyValue: TeamInput = { name: '', birthYear: new Date().getFullYear(), genderCategory: 'BOYS' };

export function TeamForm({ initialValue, submitLabel, onSubmit, onCancel }: TeamFormProps) {
  const [name, setName] = useState(initialValue?.name ?? emptyValue.name);
  const [birthYear, setBirthYear] = useState(initialValue?.birthYear ?? emptyValue.birthYear);
  const [genderCategory, setGenderCategory] = useState<GenderCategory>(
    initialValue?.genderCategory ?? emptyValue.genderCategory,
  );
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit({ name, birthYear, genderCategory });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Could not save the team');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
      {error && <ErrorMessage message={error} />}

      <div className="flex flex-col gap-1">
        <label htmlFor="team-name" className="text-sm font-medium text-slate-800">
          Team name
        </label>
        <input
          id="team-name"
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
        <label htmlFor="team-birth-year" className="text-sm font-medium text-slate-800">
          Birth year
        </label>
        <input
          id="team-birth-year"
          name="birthYear"
          type="number"
          required
          value={birthYear}
          onChange={(event) => setBirthYear(Number(event.target.value))}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="team-gender-category" className="text-sm font-medium text-slate-800">
          Gender category
        </label>
        <select
          id="team-gender-category"
          name="genderCategory"
          value={genderCategory}
          onChange={(event) => setGenderCategory(event.target.value as GenderCategory)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        >
          <option value="BOYS">Boys</option>
          <option value="GIRLS">Girls</option>
          <option value="MIXED">Mixed</option>
        </select>
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
