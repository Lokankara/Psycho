import {
  Chart as ChartJS,
  Filler,
  Legend,
  LineElement,
  PointElement,
  RadialLinearScale,
  Tooltip,
} from 'chart.js';
import { Radar } from 'react-chartjs-2';

ChartJS.register(RadialLinearScale, PointElement, LineElement, Filler, Tooltip, Legend);

interface RadarChartProps {
  labels: string[];
  values: number[];
}

export default function RadarChart({ labels, values }: RadarChartProps) {
  return (
    <Radar
      data={{
        labels,
        datasets: [
          {
            data: values,
            backgroundColor: 'rgba(229, 115, 115, 0.35)',
            borderColor: '#E57373',
            borderWidth: 2,
            pointBackgroundColor: '#E57373',
            pointBorderColor: '#ffffff',
            pointRadius: 3,
            fill: true,
          },
        ],
      }}
      options={{
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          r: {
            min: 0,
            max: 1,
            ticks: { display: false, stepSize: 0.25 },
            grid: { color: 'rgba(148, 163, 184, 0.15)' },
            angleLines: { color: 'rgba(148, 163, 184, 0.2)' },
            pointLabels: { color: '#94a3b8', font: { size: 11 } },
          },
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (context) => ` ${Math.round((context.parsed.r ?? 0) * 100)}%`,
            },
          },
        },
      }}
    />
  );
}
