import { ShieldCheck } from 'lucide-react';

export function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-950">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center gap-2 px-4 py-4 text-lg font-semibold">
          <ShieldCheck aria-hidden="true" className="h-5 w-5 text-teal-700" />
          __PROJECT_NAME__
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <h1 className="text-2xl font-semibold">First Workflow</h1>
        <p className="mt-2 max-w-2xl text-sm text-slate-700">
          Replace this with the first useful screen for the application.
        </p>
      </main>
    </div>
  );
}

