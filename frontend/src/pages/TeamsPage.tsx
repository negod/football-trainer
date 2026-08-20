import { useState } from 'react';

import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import { TeamForm } from '../features/teams/components/TeamForm';
import { TeamList } from '../features/teams/components/TeamList';
import { createTeam, listTeams, updateTeam, type Team, type TeamInput } from '../features/teams/api/teamsApi';

export function TeamsPage() {
  const [reloadKey, setReloadKey] = useState(0);
  const [editingTeam, setEditingTeam] = useState<Team | null>(null);
  const { data: teams, error, loading } = useAsync(() => listTeams(), [reloadKey]);

  async function handleCreate(input: TeamInput) {
    await createTeam(input);
    setReloadKey((key) => key + 1);
  }

  async function handleUpdate(input: TeamInput) {
    if (!editingTeam) {
      return;
    }
    await updateTeam(editingTeam.id, input);
    setEditingTeam(null);
    setReloadKey((key) => key + 1);
  }

  return (
    <div className="flex flex-col gap-8">
      <div>
        <h1 className="text-2xl font-semibold text-slate-950">Teams</h1>
        <p className="mt-1 text-sm text-slate-700">Manage the teams you coach.</p>
      </div>

      <section aria-labelledby="teams-heading" className="flex flex-col gap-3">
        <h2 id="teams-heading" className="text-lg font-medium text-slate-900">
          Your teams
        </h2>
        {loading && <LoadingMessage />}
        {error && <ErrorMessage message={error} />}
        {!loading && !error && teams && <TeamList teams={teams} onEdit={setEditingTeam} />}
      </section>

      <section aria-labelledby="team-form-heading" className="flex max-w-md flex-col gap-3">
        <h2 id="team-form-heading" className="text-lg font-medium text-slate-900">
          {editingTeam ? `Edit ${editingTeam.name}` : 'Create a team'}
        </h2>
        <TeamForm
          key={editingTeam?.id ?? 'create'}
          initialValue={editingTeam ?? undefined}
          submitLabel={editingTeam ? 'Save changes' : 'Create team'}
          onSubmit={editingTeam ? handleUpdate : handleCreate}
          onCancel={editingTeam ? () => setEditingTeam(null) : undefined}
        />
      </section>
    </div>
  );
}
