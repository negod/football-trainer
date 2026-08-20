import type { Session } from '../api/sessionsApi';
import { SessionRow } from './SessionRow';

type SessionListProps = {
  sessions: Session[];
  onSkip: (session: Session) => Promise<void>;
  onRestore: (session: Session) => Promise<void>;
  onReschedule: (session: Session, newDate: string) => Promise<void>;
};

export function SessionList({ sessions, onSkip, onRestore, onReschedule }: SessionListProps) {
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
        <SessionRow key={session.id} session={session} onSkip={onSkip} onRestore={onRestore} onReschedule={onReschedule} />
      ))}
    </ul>
  );
}
