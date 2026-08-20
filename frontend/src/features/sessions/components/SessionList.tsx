import type { Session } from '../api/sessionsApi';

type SessionListProps = {
  sessions: Session[];
};

export function SessionList({ sessions }: SessionListProps) {
  if (sessions.length === 0) {
    return (
      <p className="text-sm text-slate-600">
        No sessions yet. Choose your practice days and generate the calendar below.
      </p>
    );
  }

  return (
    <ul className="flex flex-col gap-2">
      {sessions.map((session) => (
        <li key={session.id} className="rounded-md border border-slate-200 bg-white px-4 py-3">
          <span className="font-medium text-slate-900">{session.date}</span>
        </li>
      ))}
    </ul>
  );
}
