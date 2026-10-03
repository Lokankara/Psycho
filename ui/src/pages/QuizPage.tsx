import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../api';
import type { Pole, Question } from '../types';

export default function QuizPage() {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState<Question[]>([]);
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState<Record<string, Pole>>({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .questions()
      .then(setQuestions)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-slate-400">Загрузка вопросов…</p>;
  if (error && questions.length === 0) return <p className="text-red-400">Ошибка: {error}</p>;
  if (questions.length === 0) return <p className="text-slate-400">Вопросы не найдены.</p>;

  const total = questions.length;
  const question = questions[index];
  const selected = answers[question.id];

  async function choose(pole: Pole) {
    const next = { ...answers, [question.id]: pole };
    setAnswers(next);

    if (index < total - 1) {
      setIndex(index + 1);
      return;
    }

    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        sessionId: crypto.randomUUID(),
        answers: Object.entries(next).map(([questionId, chosenPole]) => ({ questionId, chosenPole })),
      };
      const result = await api.complete(payload);
      sessionStorage.setItem('quizResult', JSON.stringify(result));
      navigate('/result');
    } catch (e) {
      setError((e as Error).message);
      setSubmitting(false);
    }
  }

  const percent = Math.round((index / total) * 100);

  return (
    <section className="mx-auto flex max-w-2xl flex-col gap-6">
      <div className="flex items-center justify-between text-sm text-slate-400">
        <span>
          Вопрос {index + 1} из {total}
        </span>
        <span>
          Ось {question.axis} · {question.title}
        </span>
      </div>

      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-800">
        <div className="h-full bg-indigo-500 transition-all" style={{ width: `${percent}%` }} />
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <button
          type="button"
          disabled={submitting}
          onClick={() => choose('NEGATIVE')}
          className={`min-h-32 rounded-2xl border p-5 text-left transition ${
            selected === 'NEGATIVE'
              ? 'border-indigo-500 bg-indigo-500/10'
              : 'border-slate-800 bg-slate-900 hover:border-slate-600'
          }`}
        >
          {question.negative}
        </button>
        <button
          type="button"
          disabled={submitting}
          onClick={() => choose('POSITIVE')}
          className={`min-h-32 rounded-2xl border p-5 text-left transition ${
            selected === 'POSITIVE'
              ? 'border-indigo-500 bg-indigo-500/10'
              : 'border-slate-800 bg-slate-900 hover:border-slate-600'
          }`}
        >
          {question.positive}
        </button>
      </div>

      {error && <p className="text-red-400">Ошибка: {error}</p>}
      {submitting && <p className="text-slate-400">Расчёт результата…</p>}

      <div className="flex justify-between">
        <button
          type="button"
          onClick={() => setIndex((current) => Math.max(0, current - 1))}
          disabled={index === 0 || submitting}
          className="rounded-lg border border-slate-700 px-4 py-2 text-sm text-slate-200 disabled:opacity-40"
        >
          Назад
        </button>
      </div>
    </section>
  );
}
