import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { octantLabel } from '../octants';
import type { HistoryEntry } from '../types';

function formatDate(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) return iso;
  return date.toLocaleString('ru-RU');
}

function formatCoord(value: number): string {
  return `${value >= 0 ? '+' : ''}${value.toFixed(2)}`;
}

export default function HistoryPage() {
  const [items, setItems] = useState<HistoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api
      .history()
      .then(setItems)
      .catch((e: Error) => setError(e.message))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="flex flex-col gap-6">
      <h1 className="text-2xl font-bold">История результатов</h1>

      {loading && <p className="text-slate-400">Загрузка…</p>}
      {error && <p className="text-red-400">Ошибка: {error}</p>}
      {!loading && !error && items.length === 0 && <p className="text-slate-400">Пока нет сохранённых результатов.</p>}

      {!loading && !error && items.length > 0 && (
        <div className="overflow-x-auto rounded-xl border border-slate-800">
          <table className="min-w-full text-sm">
            <thead className="bg-slate-900 text-slate-400">
              <tr>
                <th className="px-4 py-3 text-left">Дата</th>
                <th className="px-4 py-3 text-left">Октант</th>
                <th className="px-4 py-3 text-left">Координаты</th>
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="border-t border-slate-800">
                  <td className="px-4 py-3">{formatDate(item.createdAt)}</td>
                  <td className="px-4 py-3">{octantLabel(item.octantCode)}</td>
                  <td className="px-4 py-3 font-mono">
                    ({formatCoord(item.x)}, {formatCoord(item.y)}, {formatCoord(item.z)})
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div>
        <Link to="/quiz" className="rounded-lg bg-indigo-600 px-5 py-2.5 font-medium text-white hover:bg-indigo-500">
          Пройти тест
        </Link>
      </div>
    </section>
  );
}
