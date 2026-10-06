import type {
  AnalysisResult,
  AssessmentPayload,
  HistoryEntry,
  Question,
  QuizResultResponse,
} from './types';

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `HTTP ${response.status}`;
    try {
      const body = await response.json();
      message = body.error ?? body.message ?? message;
    } catch {
      message = response.statusText || message;
    }
    throw new Error(message);
  }
  return (await response.json()) as T;
}

const jsonHeaders = { 'Content-Type': 'application/json' };

const inflight = new Map<string, Promise<unknown>>();

/**
 * Shares one in-flight GET between concurrent callers, keyed by URL.
 * React StrictMode runs every mount effect twice in the same commit, and both
 * runs land here before either response resolves, so the server sees a single
 * request instead of two. The entry is dropped as soon as the promise settles,
 * so a later mount still fetches fresh data.
 *
 * @throws whatever {@link parse} throws for an error response, to every sharer
 */
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

export const api = {
  questions(): Promise<Question[]> {
    return get<Question[]>('/api/analysis/questions');
  },

  analyze(payload: AssessmentPayload): Promise<AnalysisResult> {
    return fetch('/api/analysis/analyze', {
      method: 'POST',
      headers: jsonHeaders,
      body: JSON.stringify(payload),
    }).then(parse<AnalysisResult>);
  },

  complete(payload: AssessmentPayload): Promise<QuizResultResponse> {
    return fetch('/api/analysis/results', {
      method: 'POST',
      headers: jsonHeaders,
      body: JSON.stringify(payload),
    }).then(parse<QuizResultResponse>);
  },

  history(): Promise<HistoryEntry[]> {
    return get<HistoryEntry[]>('/api/analysis/history');
  },
};
