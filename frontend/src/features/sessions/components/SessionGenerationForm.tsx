import { useState, type FormEvent } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { Weekday } from '../api/sessionsApi';
import { WEEKDAYS_IN_ORDER, WEEKDAY_LABELS, countMatchingDates } from '../dateMatching';

type SessionGenerationFormProps = {
  periodStartDate: string;
  periodEndDate: string;
  onGenerate: (weekdays: Weekday[]) => Promise<void>;
};

export function SessionGenerationForm({ periodStartDate, periodEndDate, onGenerate }: SessionGenerationFormProps) {
  const [selected, setSelected] = useState<Weekday[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const previewCount = countMatchingDates(periodStartDate, periodEndDate, selected);

  function toggle(weekday: Weekday) {
    setSelected((current) =>
      current.includes(weekday) ? current.filter((day) => day !== weekday) : [...current, weekday],
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onGenerate(selected);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Could not generate sessions');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
      {error && <ErrorMessage message={error} />}

      <fieldset className="flex flex-col gap-2">
        <legend className="text-sm font-medium text-slate-800">Practice days</legend>
        <div className="flex flex-wrap gap-3">
          {WEEKDAYS_IN_ORDER.map((weekday) => (
            <label key={weekday} className="flex items-center gap-1.5 text-sm text-slate-800">
              <input
                type="checkbox"
                checked={selected.includes(weekday)}
                onChange={() => toggle(weekday)}
                className="h-4 w-4 rounded border-slate-300 text-teal-700 focus:ring-teal-600"
              />
              {WEEKDAY_LABELS[weekday]}
            </label>
          ))}
        </div>
      </fieldset>

      <p className="text-sm text-slate-600">
        {selected.length === 0
          ? 'Select at least one practice day.'
          : `This will generate ${previewCount} session${previewCount === 1 ? '' : 's'}.`}
      </p>

      <button
        type="submit"
        disabled={submitting || selected.length === 0}
        className="w-fit rounded-md bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {submitting ? 'Generating...' : 'Generate sessions'}
      </button>
    </form>
  );
}
