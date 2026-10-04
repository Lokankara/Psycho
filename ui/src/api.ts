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

export const api = {
  questions(): Promise<Question[]> {
    return fetch('/api/analysis/questions').then(parse<Question[]>);
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
    return fetch('/api/analysis/history').then(parse<HistoryEntry[]>);
  },
};
