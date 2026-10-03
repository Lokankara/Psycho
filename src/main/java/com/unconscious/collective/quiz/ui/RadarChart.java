package com.unconscious.collective.quiz.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Server-side wrapper for the {@code radar-chart} Lit element
 * ({@code src/main/frontend/radar-chart.js}) built on Chart.js.
 */
@Tag("radar-chart")
public class RadarChart extends Component {

    /**
     * Pushes the radar series to the client.
     *
     * @param labels vertex labels shown in tooltips (e.g. metric names)
     * @param values one value per label, expected in {@code [0, 1]}; values are not clamped
     * @throws IllegalArgumentException if label and value counts differ
     * @throws NullPointerException if either list or any label is null
     */
    public void setChartData(List<String> labels, List<Double> values) {
        if (labels.size() != values.size()) {
            throw new IllegalArgumentException("labels and values must have the same size");
        }
        String labelJson = labels.stream()
                .map(label -> "\"" + escape(label) + "\"")
                .collect(Collectors.joining(","));
        String valueJson = values.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        getElement().setProperty(
                "chartData",
                "{\"labels\":[" + labelJson + "],\"values\":[" + valueJson + "]}");
    }

    /** Escapes backslashes and double quotes in a label; other characters are left unchanged. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}