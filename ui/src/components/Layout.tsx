import type { ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';

const links = [
  { to: '/', label: 'Главная' },
  { to: '/quiz', label: 'Тест' },
  { to: '/history', label: 'История' },
  { to: '/assessment', label: 'Анализ' },
  { to: '/board', label: 'Kanban' },
];

export default function Layout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();

  return (
    <div className="min-h-full bg-slate-950 text-slate-100">
      <header className="border-b border-slate-800">
        <nav className="mx-auto flex max-w-5xl flex-wrap items-center gap-4 px-4 py-4">
          <Link to="/" className="text-lg font-semibold tracking-tight">
            ◈ Collective Unconscious
          </Link>
          <div className="ml-auto flex gap-4 text-sm">
            {links.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={
                  link.to === pathname
                    ? 'font-medium text-indigo-400'
                    : 'text-slate-400 transition hover:text-slate-100'
                }
              >
                {link.label}
              </Link>
            ))}
          </div>
        </nav>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
