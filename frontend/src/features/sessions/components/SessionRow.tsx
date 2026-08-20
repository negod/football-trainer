import { useState } from 'react';

import { ErrorMessage } from '../../../shared/components/ErrorMessage';
import type { Session } from '../api/sessionsApi';

type SessionRowProps = {
  session: Session;
  onSkip: (session: Session) => Promise<void>;
  onRestore: (session: Session) => Promise<void>;
  onReschedule: (session: Session, newDate: string) => Promise<void>;
};

export function SessionRow({ session, onSkip, onRestore, onReschedule }: SessionRowProps) {
  const [confirmingSkip, setConfirmingSkip] = useState(false);
  const [rescheduling, setRescheduling] = useState(false);
  const [newDate, setNewDate] = useState(session.date);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function runAction(action: () => Promise<void>) {
    setError(null);
    setBusy(true);
    try {
      await action();
      setConfirmingSkip(false);
      setRescheduling(false);
    } catch (actionError) {
      setError(actionError instanceof Error ? actionError.message : 'Something went wrong');
    } finally {
      setBusy(false);
    }
  }

  return (
    <li className="flex flex-col gap-2 rounded-md border border-slate-200 bg-white px-4 py-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div>
          <span className={`font-medium ${session.status === 'SKIPPED' ? 'text-slate-400 line-through' : 'text-slate-900'}`}>
            {session.date}
          </span>
          {session.status === 'SKIPPED' && <span className="ml-2 text-sm text-slate-500">Skipped</span>}
          {session.source === 'ADHOC' && (
            <span className="ml-2 rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600">
              Ad-hoc
            </span>
          )}
        </div>

        {!confirmingSkip && !rescheduling && (
          <div className="flex gap-2">
            {session.status === 'SCHEDULED' ? (
              <button
                type="button"
                disabled={busy}
                onClick={() => setConfirmingSkip(true)}
                className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-60"
              >
                Skip
              </button>
            ) : (
              <button
                type="button"
                disabled={busy}
                onClick={() => runAction(() => onRestore(session))}
                className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-60"
              >
                Restore
              </button>
            )}
            <button
              type="button"
              disabled={busy}
              onClick={() => {
                setNewDate(session.date);
                setRescheduling(true);
              }}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100 disabled:opacity-60"
            >
              Reschedule
            </button>
          </div>
        )}
      </div>

      {confirmingSkip && (
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="text-slate-700">Skip this session?</span>
          <button
            type="button"
            disabled={busy}
            onClick={() => runAction(() => onSkip(session))}
            className="rounded-md bg-teal-700 px-3 py-1 font-medium text-white hover:bg-teal-800 disabled:opacity-60"
          >
            {busy ? 'Skipping...' : 'Confirm skip'}
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={() => setConfirmingSkip(false)}
            className="rounded-md border border-slate-300 px-3 py-1 font-medium text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
        </div>
      )}

      {rescheduling && (
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <label htmlFor={`reschedule-${session.id}`} className="text-slate-700">
            New date
          </label>
          <input
            id={`reschedule-${session.id}`}
            type="date"
            value={newDate}
            onChange={(event) => setNewDate(event.target.value)}
            className="rounded-md border border-slate-300 px-2 py-1"
          />
          <button
            type="button"
            disabled={busy}
            onClick={() => runAction(() => onReschedule(session, newDate))}
            className="rounded-md bg-teal-700 px-3 py-1 font-medium text-white hover:bg-teal-800 disabled:opacity-60"
          >
            {busy ? 'Saving...' : 'Save'}
          </button>
          <button
            type="button"
            disabled={busy}
            onClick={() => setRescheduling(false)}
            className="rounded-md border border-slate-300 px-3 py-1 font-medium text-slate-700 hover:bg-slate-100"
          >
            Cancel
          </button>
        </div>
      )}

      {error && <ErrorMessage message={error} />}
    </li>
  );
}
