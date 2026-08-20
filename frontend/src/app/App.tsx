import { ShieldCheck } from 'lucide-react';
import { Route, Routes } from 'react-router-dom';

import { TeamsPage } from '../pages/TeamsPage';
import { PlayersPage } from '../pages/PlayersPage';
import { PeriodsPage } from '../pages/PeriodsPage';

export function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center gap-2 px-4 py-4 text-lg font-semibold">
          <ShieldCheck aria-hidden="true" className="h-5 w-5 text-teal-700" />
          Coach Hub
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Routes>
          <Route path="/" element={<TeamsPage />} />
          <Route path="/teams/:teamId/players" element={<PlayersPage />} />
          <Route path="/teams/:teamId/periods" element={<PeriodsPage />} />
        </Routes>
      </main>
    </div>
  );
}
