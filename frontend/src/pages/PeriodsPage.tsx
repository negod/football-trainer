import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import { PeriodForm } from '../features/periods/components/PeriodForm';
import { PeriodList } from '../features/periods/components/PeriodList';
import {
  createPeriod,
  getSuggestedFormat,
  listPeriods,
  updatePeriod,
  type Period,
  type PeriodInput,
} from '../features/periods/api/periodsApi';
import { getTeam } from '../features/teams/api/teamsApi';

export function PeriodsPage() {
  const { teamId = '' } = useParams<{ teamId: string }>();
  const [reloadKey, setReloadKey] = useState(0);
  const [editingPeriod, setEditingPeriod] = useState<Period | null>(null);

  const { data: team, error: teamError, loading: teamLoading } = useAsync(() => getTeam(teamId), [teamId]);
  const { data: periods, error: periodsError, loading: periodsLoading } = useAsync(
    () => listPeriods(teamId),
    [teamId, reloadKey],
  );
  const { data: suggestedFormat } = useAsync(() => getSuggestedFormat(teamId), [teamId]);

  async function handleCreate(input: PeriodInput) {
    await createPeriod(teamId, input);
    setReloadKey((key) => key + 1);
  }

  async function handleUpdate(input: PeriodInput) {
    if (!editingPeriod) {
      return;
    }
    await updatePeriod(teamId, editingPeriod.id, input);
    setEditingPeriod(null);
    setReloadKey((key) => key + 1);
  }

  return (
    <div className="flex flex-col gap-8">
      <div>
        <Link to="/" className="text-sm font-medium text-teal-700 hover:underline">
          ← Back to teams
        </Link>
        <h1 className="mt-1 text-2xl font-semibold text-slate-950">
          {teamLoading && 'Loading periods...'}
          {teamError && 'Periods'}
          {team && `${team.name} periods`}
        </h1>
        <p className="mt-1 text-sm text-slate-700">Define the seasons you schedule practices into.</p>
        {teamError && <ErrorMessage message={teamError} />}
      </div>

      <section aria-labelledby="periods-heading" className="flex flex-col gap-3">
        <h2 id="periods-heading" className="text-lg font-medium text-slate-900">
          Periods
        </h2>
        {periodsLoading && <LoadingMessage />}
        {periodsError && <ErrorMessage message={periodsError} />}
        {!periodsLoading && !periodsError && periods && (
          <PeriodList periods={periods} onEdit={setEditingPeriod} />
        )}
      </section>

      <section aria-labelledby="period-form-heading" className="flex max-w-md flex-col gap-3">
        <h2 id="period-form-heading" className="text-lg font-medium text-slate-900">
          {editingPeriod ? `Edit ${editingPeriod.name}` : 'Define a period'}
        </h2>
        <PeriodForm
          key={editingPeriod?.id ?? `create-${suggestedFormat ?? 'pending'}`}
          initialValue={editingPeriod ?? undefined}
          suggestedFormat={suggestedFormat ?? undefined}
          submitLabel={editingPeriod ? 'Save changes' : 'Create period'}
          onSubmit={editingPeriod ? handleUpdate : handleCreate}
          onCancel={editingPeriod ? () => setEditingPeriod(null) : undefined}
        />
      </section>
    </div>
  );
}
