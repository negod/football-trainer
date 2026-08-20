import { useState, type FormEvent } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { MatchFormat, PeriodInput } from '../api/periodsApi';
import { MATCH_FORMAT_LABELS, MATCH_FORMATS } from '../matchFormat';

type PeriodFormProps = {
  initialValue?: PeriodInput;
  suggestedFormat?: MatchFormat;
  submitLabel: string;
  onSubmit: (input: PeriodInput) => Promise<void>;
  onCancel?: () => void;
};

function emptyValue(suggestedFormat?: MatchFormat): PeriodInput {
  const today = new Date().toISOString().slice(0, 10);
  return { name: '', startDate: today, endDate: today, format: suggestedFormat ?? 'SEVEN_V_SEVEN' };
}

export function PeriodForm({ initialValue, suggestedFormat, submitLabel, onSubmit, onCancel }: PeriodFormProps) {
  const defaults = initialValue ?? emptyValue(suggestedFormat);
  const [name, setName] = useState(defaults.name);
  const [startDate, setStartDate] = useState(defaults.startDate);
  const [endDate, setEndDate] = useState(defaults.endDate);
  const [format, setFormat] = useState<MatchFormat>(defaults.format);
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onSubmit({ name, startDate, endDate, format });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Could not save the period');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
      {error && <ErrorMessage message={error} />}

      <div className="flex flex-col gap-1">
        <label htmlFor="period-name" className="text-sm font-medium text-slate-800">
          Period name
        </label>
        <input
          id="period-name"
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
        <label htmlFor="period-start-date" className="text-sm font-medium text-slate-800">
          Start date
        </label>
        <input
          id="period-start-date"
          name="startDate"
          type="date"
          required
          value={startDate}
          onChange={(event) => setStartDate(event.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="period-end-date" className="text-sm font-medium text-slate-800">
          End date
        </label>
        <input
          id="period-end-date"
          name="endDate"
          type="date"
          required
          value={endDate}
          onChange={(event) => setEndDate(event.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="period-format" className="text-sm font-medium text-slate-800">
          Format {suggestedFormat && !initialValue && '(suggested from the team\'s age group — override if needed)'}
        </label>
        <select
          id="period-format"
          name="format"
          value={format}
          onChange={(event) => setFormat(event.target.value as MatchFormat)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        >
          {MATCH_FORMATS.map((value) => (
            <option key={value} value={value}>
              {MATCH_FORMAT_LABELS[value]}
            </option>
          ))}
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
