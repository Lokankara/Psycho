import { Link } from 'react-router-dom';
import AxisBar from '../components/AxisBar';
import RadarChart from '../components/RadarChart';
import type { QuizResultResponse } from '../types';

function readResult(): QuizResultResponse | null {
  const raw = sessionStorage.getItem('quizResult');
  if (!raw) return null;
  try {
    return JSON.parse(raw) as QuizResultResponse;
  } catch {
    return null;
  }
}

export default function ResultPage() {
  const result = readResult();

  if (!result) {
    return (
      <section className="flex flex-col items-center gap-4 text-center">
        <h2 className="text-2xl font-semibold">Результат недоступен</h2>
        <p className="text-slate-400">Сначала пройдите тест.</p>
        <Link to="/quiz" className="rounded-lg bg-indigo-600 px-5 py-2.5 font-medium text-white hover:bg-indigo-500">
          Пройти тест
        </Link>
      </section>
    );
  }

  const radarLabels = ['Социальный', 'Изменения', 'Понятийный', 'Драйв', 'Целостность', 'Мощь'];
  const radarValues = [
    (result.x + 1) / 2,
    (result.y + 1) / 2,
    (result.z + 1) / 2,
    result.confidence,
    Math.min(1, 0.5 + result.confidence / 2),
    Math.min(1, (Math.abs(result.x) + Math.abs(result.y) + Math.abs(result.z)) / 3),
  ];

  return (
    <section className="flex flex-col gap-6">
      <header className="flex flex-wrap items-center justify-between gap-3 rounded-xl border-l-4 border-indigo-500 bg-slate-900 p-5">
        <div>
          <h1 className="text-2xl font-bold">{result.archetype}</h1>
          <p className="text-sm text-slate-400">
            {result.octantName} · {result.coordinateLabel}
          </p>
        </div>
        <span className="rounded-full bg-indigo-500 px-4 py-1.5 font-semibold text-white">
          {Math.round(result.confidence * 100)}%
        </span>
      </header>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-400">Профиль осей</h2>
          <div className="flex flex-col gap-4">
            <AxisBar label="Ось X · Социальный вектор" negative="Индивидуализм" positive="Коллективизм" value={result.x} />
            <AxisBar label="Ось Y · Вектор изменений" negative="Стабильность" positive="Трансформация" value={result.y} />
            <AxisBar label="Ось Z · Понятийный вектор" negative="Материализм" positive="Абстракция" value={result.z} />
          </div>
        </div>

        <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
          <h2 className="mb-4 text-sm font-semibold uppercase tracking-wide text-slate-400">Радар смыслов</h2>
          <div className="h-72">
            <RadarChart labels={radarLabels} values={radarValues} />
          </div>
        </div>
      </div>

      <div className="grid gap-4 rounded-xl border border-slate-800 bg-slate-900 p-5 sm:grid-cols-2">
        <Info label="Смысловое содержание" value={result.meaning} />
        <Info label="Доминирующие архетипы" value={result.dominantArchetypes} />
        <Info label="Нарративная траектория" value={result.narrativeTrajectory} />
        <Info label="Тень" value={result.shadow} />
        <Info label="Ведущий мотив" value={result.driveLabel} />
        <Info label="Описание мотива" value={result.driveDescription} />
      </div>

      <div className="rounded-xl border border-slate-800 bg-slate-900 p-5">
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-slate-400">Архетипические символы</h2>
        <ul className="grid gap-3 sm:grid-cols-2">
          {result.symbols.map((symbol) => (
            <li key={symbol.name}>
              <span className="font-semibold">{symbol.name}</span>
              <span className="text-slate-400"> — {symbol.meaning}</span>
            </li>
          ))}
        </ul>
      </div>

      <div className="flex flex-wrap gap-3">
        <Link to="/quiz" className="rounded-lg bg-indigo-600 px-5 py-2.5 font-medium text-white hover:bg-indigo-500">
          Пройти снова
        </Link>
        <Link to="/history" className="rounded-lg border border-slate-700 px-5 py-2.5 font-medium text-slate-200 hover:border-slate-500">
          История
        </Link>
      </div>
    </section>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <div className="text-xs font-semibold uppercase tracking-wide text-slate-500">{label}</div>
      <div className="text-slate-200">{value}</div>
    </div>
  );
}
