import { useCallback, useEffect, useState } from 'react';
import { DndContext, KeyboardSensor, PointerSensor, closestCorners, useSensor, useSensors } from '@dnd-kit/core';
import type { DragEndEvent } from '@dnd-kit/core';
import { kanbanApi } from './api';
import BoardColumn from './BoardColumn';
import DetailDrawer from './DetailDrawer';
import KanbanCard from './KanbanCard';
import { columnIndex, moveCard, parseDragId } from './relocate';
import type { BoardResponse, BddStatus, CardKind, StoryCard, TaskCard } from './types';

interface Selected {
  kind: CardKind;
  id: number;
  code: string;
  title: string;
  points: number | null;
  bddStory: string | null;
  acceptanceCriteria: string | null;
  executionStatus: BddStatus;
  lastRunAt: string | null;
  columnCode: string;
}

function openStory(story: StoryCard, columnCode: string): Selected {
  return {
    kind: 'story',
    id: story.id,
    code: story.code,
    title: story.title,
    points: story.storyPoints,
    bddStory: story.bddStory,
    acceptanceCriteria: story.acceptanceCriteria,
    executionStatus: story.executionStatus,
    lastRunAt: story.lastRunAt,
    columnCode,
  };
}

function openTask(task: TaskCard, columnCode: string): Selected {
  return {
    kind: 'task',
    id: task.id,
    code: task.code,
    title: task.title,
    points: null,
    bddStory: task.bddStory,
    acceptanceCriteria: null,
    executionStatus: task.executionStatus,
    lastRunAt: task.lastRunAt,
    columnCode,
  };
}

export default function BoardPage() {
  const [board, setBoard] = useState<BoardResponse | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [programIncrementId, setProgramIncrementId] = useState<number | undefined>(undefined);
  const [sprintId, setSprintId] = useState<number | undefined>(undefined);
  const [selected, setSelected] = useState<Selected | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 6 } }),
    useSensor(KeyboardSensor),
  );

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    kanbanApi
      .board(programIncrementId, sprintId)
      .then((next) => {
        if (!cancelled) {
          setBoard(next);
          setError(null);
        }
      })
      .catch((cause: Error) => {
        if (!cancelled) setError(cause.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [programIncrementId, sprintId]);

  const reload = useCallback(() => {
    setLoading(true);
    kanbanApi
      .board(programIncrementId, sprintId)
      .then(setBoard)
      .catch((cause: Error) => setError(cause.message))
      .finally(() => setLoading(false));
  }, [programIncrementId, sprintId]);

  const applyMove = useCallback(
    (kind: CardKind, id: number, toCode: string) => {
      setBoard((current) => (current ? moveCard(current, { kind, id }, toCode) : current));
      setSelected((current) =>
        current && current.kind === kind && current.id === id ? { ...current, columnCode: toCode } : current,
      );
      kanbanApi.move(kind, id, toCode).catch((cause: Error) => {
        setError(cause.message);
        reload();
      });
    },
    [reload],
  );

  const handleDragEnd = useCallback(
    (event: DragEndEvent) => {
      if (!board) return;
      const { active, over } = event;
      if (!over) return;
      const reference = parseDragId(String(active.id));
      const toCode = String(over.id);
      if (!reference || !board.columns.some((column) => column.code === toCode)) return;
      applyMove(reference.kind, reference.id, toCode);
    },
    [applyMove, board],
  );

  const moveByDelta = (kind: CardKind, id: number, fromCode: string, delta: number) => {
    if (!board) return;
    const target = board.columns[columnIndex(board, fromCode) + delta];
    if (target) applyMove(kind, id, target.code);
  };

  if (loading && !board) return <p className="text-slate-400">Загрузка доски…</p>;
  if (error) return <p className="text-red-400">Ошибка: {error}</p>;
  if (!board) return <p className="text-slate-400">Данные доски недоступны.</p>;

  return (
    <section className="flex flex-col gap-5" data-testid="kanban-board">
      <header className="flex flex-wrap items-end gap-4">
        <div>
          <h1 className="text-2xl font-bold">Kanban</h1>
          <p className="text-sm text-slate-400">Перетаскивайте карточки между колонками</p>
        </div>

        <label className="ml-auto flex items-center gap-2 text-sm text-slate-400">
          PI
          <select
            value={programIncrementId ?? board.activeProgramIncrementId ?? ''}
            onChange={(event) => {
              setProgramIncrementId(event.target.value ? Number(event.target.value) : undefined);
              setSprintId(undefined);
            }}
            data-testid="pi-selector"
            className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-1.5 text-slate-100"
          >
            {board.programIncrements.map((pi) => (
              <option key={pi.id} value={pi.id}>
                {pi.code}
              </option>
            ))}
          </select>
        </label>

        <label className="flex items-center gap-2 text-sm text-slate-400">
          Sprint
          <select
            value={sprintId ?? board.activeSprintId ?? ''}
            onChange={(event) => setSprintId(event.target.value ? Number(event.target.value) : undefined)}
            data-testid="sprint-selector"
            className="rounded-lg border border-slate-800 bg-slate-900 px-3 py-1.5 text-slate-100"
          >
            {board.sprints.map((sprint) => (
              <option key={sprint.id} value={sprint.id}>
                {sprint.name}
              </option>
            ))}
          </select>
        </label>

        <button
          type="button"
          onClick={reload}
          className="rounded-lg border border-slate-700 px-3 py-1.5 text-sm text-slate-200 hover:border-slate-500"
        >
          Обновить
        </button>
      </header>

      <DndContext sensors={sensors} collisionDetection={closestCorners} onDragEnd={handleDragEnd}>
        <div className="flex gap-4 overflow-x-auto pb-4">
          {board.columns.map((column) => {
            const position = columnIndex(board, column.code);
            return (
              <BoardColumn key={column.code} column={column}>
                {column.stories.map((story) => (
                  <KanbanCard
                    key={`story:${story.id}`}
                    kind="story"
                    id={story.id}
                    code={story.code}
                    title={story.title}
                    points={story.storyPoints}
                    executionStatus={story.executionStatus}
                    canMoveLeft={position > 0}
                    canMoveRight={position < board.columns.length - 1}
                    onSelect={() => setSelected(openStory(story, column.code))}
                    onMove={(delta) => moveByDelta('story', story.id, column.code, delta)}
                  />
                ))}
                {column.tasks.map((task) => (
                  <KanbanCard
                    key={`task:${task.id}`}
                    kind="task"
                    id={task.id}
                    code={task.code}
                    title={task.title}
                    points={null}
                    executionStatus={task.executionStatus}
                    canMoveLeft={position > 0}
                    canMoveRight={position < board.columns.length - 1}
                    onSelect={() => setSelected(openTask(task, column.code))}
                    onMove={(delta) => moveByDelta('task', task.id, column.code, delta)}
                  />
                ))}
              </BoardColumn>
            );
          })}
        </div>
      </DndContext>

      {selected && board && (
        <DetailDrawer
          code={selected.code}
          title={selected.title}
          executionStatus={selected.executionStatus}
          lastRunAt={selected.lastRunAt}
          bddStory={selected.bddStory}
          acceptanceCriteria={selected.acceptanceCriteria}
          currentColumn={selected.columnCode}
          columnChoices={board.columns.map((column) => ({ code: column.code, name: column.name }))}
          onMove={(columnCode) => applyMove(selected.kind, selected.id, columnCode)}
          onClose={() => setSelected(null)}
        />
      )}
    </section>
  );
}
