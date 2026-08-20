import type { Team } from '../api/teamsApi';

type TeamListProps = {
  teams: Team[];
  onEdit: (team: Team) => void;
};

export function TeamList({ teams, onEdit }: TeamListProps) {
  if (teams.length === 0) {
    return <p className="text-sm text-slate-600">No teams yet. Create your first team below.</p>;
  }

  return (
    <ul className="flex flex-col gap-2">
      {teams.map((team) => (
        <li
          key={team.id}
          className="flex items-center justify-between rounded-md border border-slate-200 bg-white px-4 py-3"
        >
          <div>
            <span className="font-medium text-slate-900">{team.name}</span>
            <span className="ml-2 text-sm text-slate-600">{team.shorthand}</span>
          </div>
          <button
            type="button"
            onClick={() => onEdit(team)}
            className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100"
          >
            Edit
          </button>
        </li>
      ))}
    </ul>
  );
}
