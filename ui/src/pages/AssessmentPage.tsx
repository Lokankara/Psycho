import { useEffect, useState } from 'react';
import RadarChart from '../components/RadarChart';
import { api } from '../api';
import { octantLabel } from '../octants';
import type { AnalysisResult, Pole, Question } from '../types';

const poles: Pole[] = ['POSITIVE', 'NEGATIVE'];

export default function AssessmentPage() {
  const [questions, setQuestions] = useState<Question[]>([]);
  const [answers, setAnswers] = useState<Record<string, Pole>>({});
  const [result, setResult] = useState<AnalysisResult | null>(null);
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

  const ready = questions.length > 0 && questions.every((question) => answers[question.id]);

  async function submit() {
    setSubmitting(true);
    setError(null);
    try {
      const payload = {
        sessionId: crypto.randomUUID(),
        answers: questions.map((question) => ({ questionId: question.id, chosenPole: answers[question.id] })),
      };
      setResult(await api.analyze(payload));
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <p className="text-slate-400">Загрузка вопросов…</p>;
  if (error && !result) return <p className="text-red-400">Ошибка: {error}</p>;

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">Семантический анализ архетипа</h1>

      <div className="grid gap-3">
        {questions.map((question) => (
          <div key={question.id} className="rounded-xl border border-slate-800 bg-slate-900 p-4">
            <div className="mb-2 text-sm text-slate-400">
              {question.id}: оценка по оси {question.axis}
            </div>
            <div className="flex flex-wrap gap-6">
              {poles.map((pole) => (
                <label key={pole} className="flex items-center gap-2 text-sm">
                  <input
                    type="radio"
                    name={question.id}
                    checked={answers[question.id] === pole}
                    onChange={() => setAnswers((previous) => ({ ...previous, [question.id]: pole }))}
                  />
                  {pole === 'POSITIVE' ? question.positive : question.negative}
                </label>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div>
        <button
          type="button"
          onClick={submit}
          disabled={!ready || submitting}
          className="rounded-lg bg-indigo-600 px-6 py-3 font-medium text-white transition hover:bg-indigo-500 disabled:opacity-40"
        >
          {submitting ? 'Расчёт…' : 'Рассчитать результат'}
        </button>
      </div>

      {error && <p className="text-red-400">Ошибка: {error}</p>}

      {result && (
        <div className="flex flex-col gap-6">
          <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
            <h2 className="mb-2 text-lg font-semibold">{octantLabel(result.octant)}</h2>
            <p className="text-slate-400">
              Координаты ({result.position.x.toFixed(2)}, {result.position.y.toFixed(2)},{' '}
              {result.position.z.toFixed(2)}) · уверенность {Math.round(result.confidence * 100)}%
            </p>
            <p className="mt-2 text-slate-300">Тень: {result.shadowManifestation}</p>
          </div>

          <div className="h-80 rounded-xl border border-slate-800 bg-slate-900 p-5">
            <RadarChart
              labels={['Социальный', 'Изменения', 'Понятийный', 'Драйв', 'Целостность', 'Мощь']}
              values={[
                (result.position.x + 1) / 2,
                (result.position.y + 1) / 2,
                (result.position.z + 1) / 2,
                result.confidence,
                Math.min(1, 0.5 + result.confidence / 2),
                Math.min(
                  1,
                  (Math.abs(result.position.x) + Math.abs(result.position.y) + Math.abs(result.position.z)) / 3,
                ),
              ]}
            />
          </div>
        </div>
      )}
    </section>
  );
}
