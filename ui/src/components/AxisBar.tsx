interface AxisBarProps {
  label: string;
  negative: string;
  positive: string;
  value: number;
}

export default function AxisBar({ label, negative, positive, value }: AxisBarProps) {
  const percent = Math.round(((value + 1) / 2) * 100);
  const sign = value >= 0 ? '+' : '';

  return (
    <div className="w-full">
      <div className="mb-1 flex items-baseline justify-between text-xs text-slate-400">
        <span>{negative}</span>
        <span className="font-semibold text-slate-200">{label}</span>
        <span>{positive}</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-800">
        <div className="h-full rounded-full bg-indigo-500" style={{ width: `${percent}%` }} />
      </div>
      <div className="mt-1 text-right font-mono text-xs text-slate-300">
        {sign}
        {value.toFixed(2)}
      </div>
    </div>
  );
}
