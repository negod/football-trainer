import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import { AddAdhocSessionForm } from '../features/sessions/components/AddAdhocSessionForm';
import { SessionGenerationForm } from '../features/sessions/components/SessionGenerationForm';
import { SessionList } from '../features/sessions/components/SessionList';
import {
  addAdhocSession,
  generateSessions,
  listSessions,
  updateSession,
  type Session,
  type Weekday,
} from '../features/sessions/api/sessionsApi';
import { getPeriod } from '../features/periods/api/periodsApi';

export function SessionsPage() {
  const { teamId = '', periodId = '' } = useParams<{ teamId: string; periodId: string }>();
  const [reloadKey, setReloadKey] = useState(0);

  const { data: period, error: periodError, loading: periodLoading } = useAsync(
    () => getPeriod(teamId, periodId),
    [teamId, periodId],
  );
  const { data: sessions, error: sessionsError, loading: sessionsLoading } = useAsync(
    () => listSessions(teamId, periodId),
    [teamId, periodId, reloadKey],
  );

  async function handleGenerate(weekdays: Weekday[]) {
    await generateSessions(teamId, periodId, weekdays);
    setReloadKey((key) => key + 1);
  }

  async function handleAddAdhoc(date: string) {
    await addAdhocSession(teamId, periodId, date);
    setReloadKey((key) => key + 1);
  }

  async function handleSkip(session: Session) {
    await updateSession(teamId, periodId, session.id, { status: 'SKIPPED' });
    setReloadKey((key) => key + 1);
  }

  async function handleRestore(session: Session) {
    await updateSession(teamId, periodId, session.id, { status: 'SCHEDULED' });
    setReloadKey((key) => key + 1);
  }

  async function handleReschedule(session: Session, newDate: string) {
    await updateSession(teamId, periodId, session.id, { date: newDate });
    setReloadKey((key) => key + 1);
  }

  return (
    <div className="flex flex-col gap-8">
      <div>
        <Link to={`/teams/${teamId}/periods`} className="text-sm font-medium text-teal-700 hover:underline">
          ← Back to periods
        </Link>
        <h1 className="mt-1 text-2xl font-semibold text-slate-950">
          {periodLoading && 'Loading sessions...'}
          {periodError && 'Sessions'}
          {period && `${period.name} sessions`}
        </h1>
        <p className="mt-1 text-sm text-slate-700">Generate the season's practice calendar from recurring weekdays.</p>
        {periodError && <ErrorMessage message={periodError} />}
      </div>

      <section aria-labelledby="sessions-heading" className="flex flex-col gap-3">
        <h2 id="sessions-heading" className="text-lg font-medium text-slate-900">
          Sessions
        </h2>
        {sessionsLoading && <LoadingMessage />}
        {sessionsError && <ErrorMessage message={sessionsError} />}
        {!sessionsLoading && !sessionsError && sessions && (
          <SessionList
            sessions={sessions}
            onSkip={handleSkip}
            onRestore={handleRestore}
            onReschedule={handleReschedule}
          />
        )}
      </section>

      {period && (
        <section aria-labelledby="generate-heading" className="flex max-w-md flex-col gap-3">
          <h2 id="generate-heading" className="text-lg font-medium text-slate-900">
            Generate sessions
          </h2>
          <SessionGenerationForm
            periodStartDate={period.startDate}
            periodEndDate={period.endDate}
            onGenerate={handleGenerate}
          />
        </section>
      )}

      <section aria-labelledby="adhoc-heading" className="flex max-w-md flex-col gap-3">
        <h2 id="adhoc-heading" className="text-lg font-medium text-slate-900">
          Add a one-off session
        </h2>
        <AddAdhocSessionForm onAdd={handleAddAdhoc} />
      </section>
    </div>
  );
}
