import { Link } from 'react-router-dom';

import type { Period } from '../api/periodsApi';
import { MATCH_FORMAT_LABELS } from '../matchFormat';

type PeriodListProps = {
  teamId: string;
  periods: Period[];
  onEdit: (period: Period) => void;
};

export function PeriodList({ teamId, periods, onEdit }: PeriodListProps) {
  if (periods.length === 0) {
    return <p className="text-sm text-slate-600">No periods yet. Define your first season below.</p>;
  }

  return (
    <ul className="flex flex-col gap-2">
      {periods.map((period) => (
        <li
          key={period.id}
          className="flex items-center justify-between rounded-md border border-slate-200 bg-white px-4 py-3"
        >
          <div>
            <span className="font-medium text-slate-900">{period.name}</span>
            <span className="ml-2 text-sm text-slate-600">
              {period.startDate} – {period.endDate} · {MATCH_FORMAT_LABELS[period.format]}
            </span>
          </div>
          <div className="flex gap-2">
            <Link
              to={`/teams/${teamId}/periods/${period.id}/sessions`}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100"
            >
              Sessions
            </Link>
            <button
              type="button"
              onClick={() => onEdit(period)}
              className="rounded-md border border-slate-300 px-3 py-1 text-sm font-medium text-slate-700 hover:bg-slate-100"
            >
              Edit
            </button>
          </div>
        </li>
      ))}
    </ul>
  );
}
