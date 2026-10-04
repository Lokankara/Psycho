import { useDraggable } from '@dnd-kit/core';
import BddStatusBadge from './BddStatusBadge';
import type { BddStatus, CardKind } from './types';

export interface KanbanCardProps {
  kind: CardKind;
  id: number;
  code: string;
  title: string;
  points: number | null;
  executionStatus: BddStatus;
  canMoveLeft: boolean;
  canMoveRight: boolean;
  onSelect: () => void;
  onMove: (delta: -1 | 1) => void;
}

export default function KanbanCard(props: KanbanCardProps) {
  const { kind, id, code, title, points, executionStatus } = props;
  const draggableId = `${kind}:${id}`;
  const { setNodeRef, listeners, attributes, isDragging } = useDraggable({ id: draggableId });

  return (
    <article
      ref={setNodeRef}
      className={`rounded-lg border border-slate-800 bg-slate-950 p-3 transition ${
        isDragging ? 'opacity-40' : 'hover:border-slate-700'
      }`}
      data-testid={`kanban-card-${draggableId}`}
    >
      <div className="flex items-start gap-2">
        <span
          {...listeners}
          {...attributes}
          role="button"
          tabIndex={0}
          aria-label={`Drag ${code} ${title}`}
          className="cursor-grab select-none pt-1 text-slate-500 hover:text-slate-300 active:cursor-grabbing"
        >
          ⠿
        </span>
        <button type="button" onClick={props.onSelect} className="min-w-0 flex-1 text-left">
          <span className="block text-[0.7rem] font-medium uppercase tracking-wide text-slate-500">{code}</span>
          <span className="block text-sm font-medium text-slate-100">{title}</span>
        </button>
        <BddStatusBadge status={executionStatus} />
      </div>

      <div className="mt-3 flex items-center justify-between">
        <span className="text-xs text-slate-500">{points != null ? `${points} pt` : kind === 'task' ? 'task' : ''}</span>
        <div className="flex gap-1">
          <button
            type="button"
            aria-label={`Move ${code} to previous column`}
            disabled={!props.canMoveLeft}
            onClick={() => props.onMove(-1)}
            className="rounded border border-slate-800 px-2 py-0.5 text-slate-400 transition hover:border-slate-600 hover:text-slate-200 disabled:opacity-30"
          >
            ←
          </button>
          <button
            type="button"
            aria-label={`Move ${code} to next column`}
            disabled={!props.canMoveRight}
            onClick={() => props.onMove(1)}
            className="rounded border border-slate-800 px-2 py-0.5 text-slate-400 transition hover:border-slate-600 hover:text-slate-200 disabled:opacity-30"
          >
            →
          </button>
        </div>
      </div>
    </article>
  );
}
