import { useState, type FormEvent } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';

type AddAdhocSessionFormProps = {
  onAdd: (date: string) => Promise<void>;
};

export function AddAdhocSessionForm({ onAdd }: AddAdhocSessionFormProps) {
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await onAdd(date);
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Could not add the session');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4" noValidate>
      {error && <ErrorMessage message={error} />}

      <div className="flex flex-col gap-1">
        <label htmlFor="adhoc-session-date" className="text-sm font-medium text-slate-800">
          Date
        </label>
        <input
          id="adhoc-session-date"
          name="date"
          type="date"
          required
          value={date}
          onChange={(event) => setDate(event.target.value)}
          className="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-teal-600 focus:outline-none focus:ring-1 focus:ring-teal-600"
        />
      </div>

      <button
        type="submit"
        disabled={submitting}
        className="w-fit rounded-md bg-teal-700 px-4 py-2 text-sm font-medium text-white hover:bg-teal-800 disabled:opacity-60"
      >
        {submitting ? 'Adding...' : 'Add session'}
      </button>
    </form>
  );
}
