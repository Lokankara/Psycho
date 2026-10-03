import { LitElement, css, html } from 'lit';
import Chart from 'chart.js/auto';

/**
 * Lit wrapper around a Chart.js radar chart.
 *
 * Expects a single `chartData` property holding a JSON payload:
 *   { "labels": ["...", ...], "values": [0.0 .. 1.0, ...] }
 *
 * Renders a filled, semi-transparent polygon (#E57373) on a light HUD-style
 * grid. Vertex labels are hidden on purpose — the surrounding metric chips
 * in AssessmentView provide the labelled perimeter.
 */
class RadarChart extends LitElement {
    static properties = {
        chartData: { type: String }
    };

    static styles = css`
        :host {
            display: block;
            width: 100%;
            height: 100%;
        }

        .frame {
            position: relative;
            width: 100%;
            height: 100%;
            min-height: 300px;
        }
    `;

    render() {
        return html`<div class="frame"><canvas></canvas></div>`;
    }

    updated() {
        this._draw();
    }

    disconnectedCallback() {
        this._destroyChart();
        super.disconnectedCallback();
    }

    _destroyChart() {
        if (this._chart) {
            this._chart.destroy();
            this._chart = null;
        }
    }

    _draw() {
        this._destroyChart();

        if (!this.chartData) {
            return;
        }

        let payload;
        try {
            payload = JSON.parse(this.chartData);
        } catch (e) {
            return;
        }
        if (!payload.labels || !payload.labels.length || !payload.values) {
            return;
        }

        const canvas = this.renderRoot.querySelector('canvas');
        if (!canvas) {
            return;
        }

        this._chart = new Chart(canvas, {
            type: 'radar',
            data: {
                labels: payload.labels,
                datasets: [
                    {
                        data: payload.values,
                        backgroundColor: 'rgba(229, 115, 115, 0.35)',
                        borderColor: '#E57373',
                        borderWidth: 2,
                        pointBackgroundColor: '#E57373',
                        pointBorderColor: '#FFFFFF',
                        pointRadius: 3,
                        pointHoverRadius: 5,
                        fill: true
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                animation: { duration: 400 },
                scales: {
                    r: {
                        min: 0,
                        max: 1,
                        ticks: {
                            display: false,
                            stepSize: 0.25
                        },
                        grid: { color: 'rgba(0, 0, 0, 0.08)' },
                        angleLines: { color: 'rgba(0, 0, 0, 0.10)' },
                        pointLabels: { display: false }
                    }
                },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (ctx) => ` ${Math.round(ctx.parsed.r * 100)}%`
                        }
                    }
                }
            }
        });
    }
}

customElements.define('radar-chart', RadarChart);
