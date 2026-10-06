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

function labelClass(status: BddStatus): string {
  if (status === 'FAILED') return 'label-red';
  if (status === 'PASSED') return 'label-green';
  return 'label-yellow';
}

export default function KanbanCard(props: KanbanCardProps) {
  const { kind, id, code, title, points, executionStatus } = props;
  const draggableId = `${kind}:${id}`;
  const { setNodeRef, listeners, attributes, isDragging } = useDraggable({ id: draggableId });

  return (
    <div
      ref={setNodeRef}
      {...listeners}
      {...attributes}
      role="group"
      aria-label={`Drag ${code} ${title}`}
      className={`kanban-card ${isDragging ? 'dragging' : ''}`}
      data-testid={`kanban-card-${draggableId}`}
      draggable="true"
      data-id={draggableId}
    >
      <div className={`card-label ${labelClass(executionStatus)}`} />
      <div className="card-id-title">{title}</div>
      <footer className="card-footer">
        <span>{code}</span>
        <span>
          Dark Factory <BddStatusBadge status={executionStatus} />
        </span>
      </footer>
    </div>
  );
}
