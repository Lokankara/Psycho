import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';

const axes = [
  { code: 'X', title: 'Социальный вектор', negative: 'Индивидуализм', positive: 'Коллективизм' },
  { code: 'Y', title: 'Вектор изменений', negative: 'Стабильность', positive: 'Трансформация' },
  { code: 'Z', title: 'Понятийный вектор', negative: 'Материализм', positive: 'Абстракция' },
];

export default function WelcomePage() {
  const [count, setCount] = useState<number | null>(null);

  useEffect(() => {
    api
      .questions()
      .then((questions) => setCount(questions.length))
      .catch(() => setCount(null));
  }, []);

  return (
    <section className="flex flex-col items-center gap-8 text-center">
      <div className="space-y-3">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Коллективное бессознательное</h1>
        <p className="text-slate-400">Трёхмерная система смысловых координат (X, Y, Z)</p>
      </div>

      <p className="max-w-2xl text-slate-300">
        Ответьте на {count ?? '—'} пар утверждений, выбирая то, что вам ближе. На основе ваших ответов
        система построит точку в смысловом пространстве и определит доминирующий архетип.
      </p>

      <div className="grid w-full gap-4 sm:grid-cols-3">
        {axes.map((axis) => (
          <div key={axis.code} className="rounded-xl border border-slate-800 bg-slate-900 p-4 text-left">
            <div className="mb-2 font-semibold">
              Ось {axis.code} — {axis.title}
            </div>
            <div className="text-sm text-slate-400">
              {axis.negative} ⟷ {axis.positive}
            </div>
          </div>
        ))}
      </div>

      <div className="flex flex-wrap justify-center gap-3">
        <Link to="/quiz" className="rounded-lg bg-indigo-600 px-6 py-3 font-medium text-white transition hover:bg-indigo-500">
          Начать тест
        </Link>
        <Link to="/history" className="rounded-lg border border-slate-700 px-6 py-3 font-medium text-slate-200 transition hover:border-slate-500">
          История результатов
        </Link>
      </div>
    </section>
  );
}
