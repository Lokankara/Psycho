import type { BddStatus } from './types';

const label: Record<BddStatus, string> = {
  NOT_RUN: 'Not run',
  PASSED: 'Passed',
  FAILED: 'Failed',
};

const tone: Record<BddStatus, string> = {
  NOT_RUN: 'bg-slate-800 text-slate-400',
  PASSED: 'bg-emerald-500/15 text-emerald-400',
  FAILED: 'bg-red-500/15 text-red-400',
};

export default function BddStatusBadge({ status }: { status: BddStatus }) {
  return (
    <span
      className={`shrink-0 rounded-full px-2 py-0.5 text-[0.65rem] font-semibold uppercase tracking-wide ${tone[status]}`}
      title={`BDD execution status: ${label[status]}`}
    >
      {label[status]}
    </span>
  );
}
