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
    <div className="drawer open" id="details-drawer">
      <div className="drawer-header">
        <h2 id="drawer-title" className="text-slate-100">{props.code}</h2>
        <button className="close-btn" id="close-drawer-btn" aria-label="Close details" onClick={props.onClose}>
          ×
        </button>
      </div>

      <h3 className="text-slate-400">{props.title}</h3>

      <div className="flex items-center gap-2 text-sm">
        <BddStatusBadge status={props.executionStatus} />
        {props.bddStory && <span className="text-slate-400">{props.bddStory}</span>}
      </div>

      {props.lastRunAt && (
        <p className="text-xs text-slate-500">Last run: {new Date(props.lastRunAt).toLocaleString('ru-RU')}</p>
      )}

      <pre className="json-viewer" id="drawer-json">
        {JSON.stringify(
          {
            id: props.code,
            title: props.title,
            executionStatus: props.executionStatus,
            lastRunAt: props.lastRunAt,
            bddStory: props.bddStory,
            acceptanceCriteria: props.acceptanceCriteria,
          },
          null,
          2,
        )}
      </pre>

      <section>
        <h3 className="mb-2 text-xs font-semibold uppercase tracking-widest text-slate-400">
          Move to
        </h3>
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
    </div>
  );
}
