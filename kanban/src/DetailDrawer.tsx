import BddStatusBadge from './BddStatusBadge';
import type { BddStatus } from './types';

export interface ColumnChoice {
  code: string;
  name: string;
}

export interface DetailDrawerProps {
  code: string;
  title: string;
  executionStatus: BddStatus;
  lastRunAt: string | null;
  bddStory: string | null;
  acceptanceCriteria: string | null;
  currentColumn: string;
  columnChoices: ColumnChoice[];
  onMove: (columnCode: string) => void;
  onClose: () => void;
}

export default function DetailDrawer(props: DetailDrawerProps) {
  const criteria = (props.acceptanceCriteria ?? '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  return (
    <div
      className="fixed inset-0 z-40 flex justify-end bg-black/60"
      role="dialog"
      aria-modal="true"
      aria-label={`${props.code} details`}
      onClick={props.onClose}
    >
      <aside
        className="flex h-full w-full max-w-md flex-col gap-4 overflow-y-auto border-l border-slate-800 bg-slate-950 p-6"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="flex items-start justify-between gap-3">
          <div>
            <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{props.code}</p>
            <h2 className="text-lg font-semibold text-slate-100">{props.title}</h2>
          </div>
          <button
            type="button"
            onClick={props.onClose}
            aria-label="Close details"
            className="rounded border border-slate-800 px-2 py-1 text-slate-400 hover:border-slate-600 hover:text-slate-200"
          >
            ✕
          </button>
        </div>

        <div className="flex items-center gap-2 text-sm">
          <BddStatusBadge status={props.executionStatus} />
          {props.bddStory && <span className="text-slate-400">{props.bddStory}</span>}
        </div>

        {props.lastRunAt && (
          <p className="text-xs text-slate-500">Last run: {new Date(props.lastRunAt).toLocaleString('ru-RU')}</p>
        )}

        <section>
          <h3 className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-400">
            Acceptance criteria
          </h3>
          {criteria.length === 0 ? (
            <p className="text-sm text-slate-500">No criteria recorded.</p>
          ) : (
            <ul className="space-y-2">
              {criteria.map((line, index) => (
                <li key={index} className="rounded border border-slate-800 bg-slate-900 px-3 py-2 text-sm text-slate-200">
                  {line}
                </li>
              ))}
            </ul>
          )}
        </section>

        <section>
          <h3 className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-400">Move to</h3>
          <div className="flex flex-wrap gap-2">
            {props.columnChoices.map((choice) => (
              <button
                key={choice.code}
                type="button"
                disabled={choice.code === props.currentColumn}
                onClick={() => props.onMove(choice.code)}
                className={`rounded-lg border px-3 py-1.5 text-sm transition ${
                  choice.code === props.currentColumn
                    ? 'border-indigo-500 bg-indigo-500/15 text-indigo-300'
                    : 'border-slate-800 text-slate-300 hover:border-slate-600'
                }`}
              >
                {choice.name}
              </button>
            ))}
          </div>
        </section>
      </aside>
    </div>
  );
}
