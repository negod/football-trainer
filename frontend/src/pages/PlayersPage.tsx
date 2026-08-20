import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';

import { ErrorMessage } from '../shared/components/ErrorMessage';
import { LoadingMessage } from '../shared/components/LoadingMessage';
import { useAsync } from '../shared/hooks/useAsync';
import { PlayerForm } from '../features/players/components/PlayerForm';
import { PlayerList } from '../features/players/components/PlayerList';
import {
  createPlayer,
  deletePlayer,
  listPlayers,
  updatePlayer,
  type Player,
  type PlayerInput,
} from '../features/players/api/playersApi';
import { getTeam } from '../features/teams/api/teamsApi';

export function PlayersPage() {
  const { teamId = '' } = useParams<{ teamId: string }>();
  const [reloadKey, setReloadKey] = useState(0);
  const [editingPlayer, setEditingPlayer] = useState<Player | null>(null);
  const [removeError, setRemoveError] = useState<string | null>(null);

  const { data: team, error: teamError, loading: teamLoading } = useAsync(() => getTeam(teamId), [teamId]);
  const { data: players, error: playersError, loading: playersLoading } = useAsync(
    () => listPlayers(teamId),
    [teamId, reloadKey],
  );

  async function handleCreate(input: PlayerInput) {
    await createPlayer(teamId, input);
    setReloadKey((key) => key + 1);
  }

  async function handleUpdate(input: PlayerInput) {
    if (!editingPlayer) {
      return;
    }
    await updatePlayer(teamId, editingPlayer.id, input);
    setEditingPlayer(null);
    setReloadKey((key) => key + 1);
  }

  async function handleRemove(player: Player) {
    setRemoveError(null);
    try {
      await deletePlayer(teamId, player.id);
      if (editingPlayer?.id === player.id) {
        setEditingPlayer(null);
      }
      setReloadKey((key) => key + 1);
    } catch (error) {
      setRemoveError(error instanceof Error ? error.message : 'Could not remove the player');
    }
  }

  return (
    <div className="flex flex-col gap-8">
      <div>
        <Link to="/" className="text-sm font-medium text-teal-700 hover:underline">
          ← Back to teams
        </Link>
        <h1 className="mt-1 text-2xl font-semibold text-slate-950">
          {teamLoading && 'Loading roster...'}
          {teamError && 'Roster'}
          {team && `${team.name} roster`}
        </h1>
        <p className="mt-1 text-sm text-slate-700">Manage the players on this team.</p>
        {teamError && <ErrorMessage message={teamError} />}
      </div>

      <section aria-labelledby="players-heading" className="flex flex-col gap-3">
        <h2 id="players-heading" className="text-lg font-medium text-slate-900">
          Players
        </h2>
        {removeError && <ErrorMessage message={removeError} />}
        {playersLoading && <LoadingMessage />}
        {playersError && <ErrorMessage message={playersError} />}
        {!playersLoading && !playersError && players && (
          <PlayerList players={players} onEdit={setEditingPlayer} onRemove={handleRemove} />
        )}
      </section>

      <section aria-labelledby="player-form-heading" className="flex max-w-md flex-col gap-3">
        <h2 id="player-form-heading" className="text-lg font-medium text-slate-900">
          {editingPlayer ? `Edit ${editingPlayer.name}` : 'Add a player'}
        </h2>
        <PlayerForm
          key={editingPlayer?.id ?? 'create'}
          initialValue={editingPlayer ?? undefined}
          submitLabel={editingPlayer ? 'Save changes' : 'Add player'}
          onSubmit={editingPlayer ? handleUpdate : handleCreate}
          onCancel={editingPlayer ? () => setEditingPlayer(null) : undefined}
        />
      </section>
    </div>
  );
}
