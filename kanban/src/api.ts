import type { BoardResponse, CardKind, StoryCard, TaskCard } from './types';

const jsonHeaders = { 'Content-Type': 'application/json' };

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new Error(`HTTP ${response.status}`);
  }
  return (await response.json()) as T;
}

const inflight = new Map<string, Promise<unknown>>();

function get<T>(url: string): Promise<T> {
  const pending = inflight.get(url);
  if (pending) {
    return pending as Promise<T>;
  }
  const request = fetch(url)
    .then(parse<T>)
    .finally(() => {
      inflight.delete(url);
    });
  inflight.set(url, request);
  return request;
}

function boardUrl(programIncrementId?: number, sprintId?: number): string {
  const params = new URLSearchParams();
  if (programIncrementId) params.set('programIncrementId', String(programIncrementId));
  if (sprintId) params.set('sprintId', String(sprintId));
  const query = params.toString();
  return query ? `/api/agile/board?${query}` : '/api/agile/board';
}

export const kanbanApi = {
  board(programIncrementId?: number, sprintId?: number): Promise<BoardResponse> {
    return get<BoardResponse>(boardUrl(programIncrementId, sprintId));
  },

  moveStory(id: number, columnCode: string): Promise<StoryCard> {
    return fetch(`/api/agile/stories/${id}/column`, {
      method: 'PATCH',
      headers: jsonHeaders,
      body: JSON.stringify({ columnCode }),
    }).then(parse<StoryCard>);
  },

  moveTask(id: number, columnCode: string): Promise<TaskCard> {
    return fetch(`/api/agile/tasks/${id}/column`, {
      method: 'PATCH',
      headers: jsonHeaders,
      body: JSON.stringify({ columnCode }),
    }).then(parse<TaskCard>);
  },

  move(kind: CardKind, id: number, columnCode: string): Promise<unknown> {
    return kind === 'story' ? kanbanApi.moveStory(id, columnCode) : kanbanApi.moveTask(id, columnCode);
  },
};
