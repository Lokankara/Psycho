import type { BoardResponse, CardKind, StoryCard, TaskCard } from './types';

export interface CardRef {
  kind: CardKind;
  id: number;
}

export function parseDragId(rawId: string): CardRef | null {
  const separator = rawId.indexOf(':');
  if (separator < 0) return null;
  const kind = rawId.slice(0, separator);
  const id = Number(rawId.slice(separator + 1));
  if ((kind !== 'story' && kind !== 'task') || Number.isNaN(id)) return null;
  return { kind, id };
}

export function columnIndex(board: BoardResponse, code: string): number {
  return board.columns.findIndex((column) => column.code === code);
}

export function locate(board: BoardResponse, ref: CardRef): string | null {
  for (const column of board.columns) {
    const found =
      ref.kind === 'story'
        ? column.stories.some((story) => story.id === ref.id)
        : column.tasks.some((task) => task.id === ref.id);
    if (found) return column.code;
  }
  return null;
}

export function moveCard(board: BoardResponse, ref: CardRef, toCode: string): BoardResponse {
  const fromCode = locate(board, ref);
  if (fromCode === null || fromCode === toCode) return board;

  let story: StoryCard | undefined;
  let task: TaskCard | undefined;

  for (const column of board.columns) {
    if (column.code === fromCode) {
      if (ref.kind === 'story') {
        story = column.stories.find((item) => item.id === ref.id);
      } else {
        task = column.tasks.find((item) => item.id === ref.id);
      }
      break;
    }
  }

  if (!story && !task) return board;

  return {
    ...board,
    columns: board.columns.map((column) => {
      if (column.code === fromCode) {
        return {
          ...column,
          stories: column.stories.filter((item) => item.id !== ref.id),
          tasks: column.tasks.filter((item) => item.id !== ref.id),
        };
      }
      if (column.code === toCode) {
        return {
          ...column,
          stories: story ? [...column.stories, story] : column.stories,
          tasks: task ? [...column.tasks, task] : column.tasks,
        };
      }
      return column;
    }),
  };
}
