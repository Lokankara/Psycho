import type { ReactNode } from 'react';
import { useDroppable } from '@dnd-kit/core';
import type { KanbanColumn } from './types';

interface BoardColumnProps {
  column: KanbanColumn;
  children: ReactNode;
}

export default function BoardColumn({ column, children }: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: column.code });
  const total = column.stories.length + column.tasks.length;

  return (
    <section
      className={`flex w-72 shrink-0 flex-col rounded-xl border transition ${
        isOver ? 'border-indigo-500 bg-indigo-500/10' : 'border-slate-800 bg-slate-900'
      }`}
      aria-label={`${column.name} column`}
      data-testid={`board-column-${column.code.toLowerCase()}`}
    >
      <header className="flex items-center justify-between border-b border-slate-800 px-4 py-3">
        <h2 className="text-xs font-semibold uppercase tracking-widest text-slate-300">{column.name}</h2>
        <span className="rounded-full bg-slate-800 px-2 py-0.5 text-xs text-slate-400">{total}</span>
      </header>
      <div ref={setNodeRef} className="flex min-h-44 flex-col gap-3 p-3">
        {children}
      </div>
    </section>
  );
}
