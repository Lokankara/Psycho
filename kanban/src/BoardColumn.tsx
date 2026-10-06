import type { ReactNode } from 'react';
import { useDroppable } from '@dnd-kit/core';
import type { KanbanColumn as KanbanColumnType } from './types';

interface BoardColumnProps {
  column: KanbanColumnType;
  children: ReactNode;
}

export default function BoardColumn({ column, children }: BoardColumnProps) {
  const { setNodeRef, isOver } = useDroppable({ id: column.code });
  const total = column.stories.length + column.tasks.length;

  return (
    <div
      ref={setNodeRef}
      className={`kanban-column ${isOver ? 'drag-over' : ''}`}
      data-testid={`board-column-${column.code.toLowerCase()}`}
      data-status={column.name}
      id={`col_${column.code.toLowerCase()}`}
      aria-label={`${column.name} column`}
    >
      <div className="column-header">
        <h2 className="column-title">{column.name}</h2>
        <span className="cards-count">{total}</span>
      </div>
      <div className="card-list">{children}</div>
    </div>
  );
}
