import type { ReactNode } from 'react';

const MAIN_APP = 'http://localhost:5173';

const links = [
  { href: `${MAIN_APP}/`, label: 'Главная' },
  { href: `${MAIN_APP}/quiz`, label: 'Тест' },
  { href: `${MAIN_APP}/history`, label: 'История' },
  { href: `${MAIN_APP}/assessment`, label: 'Анализ' },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  return (
    <div className="min-h-full bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800">
        <nav className="mx-auto flex max-w-5xl flex-wrap items-center gap-4 px-4 py-4">
          <a href={MAIN_APP} className="text-lg font-semibold tracking-tight" data-discover="true">
            ◈ Collective Unconscious
          </a>
          <div className="ml-auto flex gap-4 text-sm">
            {links.map((link) => (
              <a
                key={link.href}
                href={link.href}
                data-discover="true"
                className="text-slate-400 transition hover:text-slate-100"
              >
                {link.label}
              </a>
            ))}
            <span className="font-medium text-indigo-400">Kanban</span>
          </div>
        </nav>
      </header>
      <main className="mx-auto">{children}</main>
    </div>
  );
}
