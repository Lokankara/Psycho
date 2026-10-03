package com.unconscious.collective.quiz.ui;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.dto.AnalysisResult;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.symbol.Symbol;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.value.Vector3D;
import com.unconscious.collective.quiz.service.QuizService;
import com.unconscious.collective.quiz.service.ResultService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Route("assessment")
@PageTitle("Семантический анализ архетипа")
public class AssessmentView extends VerticalLayout {

    private static final String ACCENT = "#E57373";
    private static final Logger LOG = LoggerFactory.getLogger(AssessmentView.class);

    private final ResultService resultService;
    private final List<BipolarQuestion> questionsBank;
    private final Map<String, RadioButtonGroup<Pole>> answerInputs = new HashMap<>();
    private final VerticalLayout resultContainer = new VerticalLayout();

    public AssessmentView(ResultService resultService, QuizService quizService) {
        this.resultService = resultService;
        this.questionsBank = quizService.questions();

        setSpacing(true);
        setPadding(true);
        setMaxWidth("1100px");

        add(new H2("Семантический Анализ Архетипа"));
        buildQuizForm();

        Button submitButton = new Button("Рассчитать Результат", e -> processSubmission());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        resultContainer.setPadding(false);
        resultContainer.setSpacing(true);
        resultContainer.setWidth("100%");

        add(submitButton, resultContainer);
    }

    private void buildQuizForm() {
        for (BipolarQuestion question : questionsBank) {
            VerticalLayout questionBlock = new VerticalLayout();
            questionBlock.setSpacing(false);
            questionBlock.setPadding(false);

            RadioButtonGroup<Pole> poleGroup = new RadioButtonGroup<>();
            poleGroup.setLabel(question.id() + ": Оценка по оси " + question.axis());
            poleGroup.setItems(Pole.POSITIVE, Pole.NEGATIVE);
            poleGroup.setItemLabelGenerator(pole -> pole == Pole.POSITIVE ? "Положительный (+1)" : "Отрицательный (-1)");

            answerInputs.put(question.id(), poleGroup);
            questionBlock.add(poleGroup);
            add(questionBlock);
        }
    }

    /**
     * Requires all questions to be answered, then analyzes the choices and replaces
     * the displayed result. Incomplete answers and runtime failures from analysis
     * produce error notifications and leave any previous result displayed.
     */
    private void processSubmission() {
        List<QuizAnswer> answers = new ArrayList<>();

        for (Map.Entry<String, RadioButtonGroup<Pole>> entry : answerInputs.entrySet()) {
            Pole selectedPole = entry.getValue().getValue();
            if (selectedPole != null) {
                answers.add(new QuizAnswer(entry.getKey(), selectedPole));
            }
        }

        if (answers.size() < questionsBank.size()) {
            Notification notification = Notification.show("Ответьте на все вопросы перед отправкой");
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        AssessmentPayload payload = new AssessmentPayload(UUID.randomUUID().toString(), answers);

        AnalysisResult result;
        try {
            result = resultService.analyze(payload, questionsBank);
        } catch (ResponseStatusException e) {
            showError(e.getReason() != null ? e.getReason() : e.getMessage());
            return;
        } catch (RuntimeException e) {
            LOG.error("Analysis failed for session {}", payload.sessionId(), e);
            showError("Не удалось выполнить анализ");
            return;
        }

        displayResult(result);
    }

    /** Displays an error notification in the middle of the screen for five seconds. */
    private static void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    /** Replaces the result area with the analysis header, radar chart, details, and object ID. */
    private void displayResult(AnalysisResult result) {
        resultContainer.removeAll();

        Component sidePanel = buildSidePanel(result);
        HorizontalLayout dashboard = new HorizontalLayout(buildRadarCard(result), sidePanel);
        dashboard.setWidth("100%");
        dashboard.setAlignItems(Alignment.STRETCH);
        dashboard.setSpacing(true);
        dashboard.expand(sidePanel);

        resultContainer.add(buildHeader(result), dashboard, footer(result));
    }

    /** Header: archetype title, octant label, confidence badge. */
    private static Component buildHeader(AnalysisResult result) {
        H3 title = new H3(result.dominant().getTitle());
        title.getStyle()
                .set("margin", "0")
                .set("font-size", "1.2rem")
                .set("font-weight", "700")
                .set("color", "var(--lumo-text-color)");

        Octant octant = result.octant();
        Span octantLabel = new Span(octant.octantName() + "  ·  " + octant.coordinateLabel());
        octantLabel.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout titleBox = new VerticalLayout(title, octantLabel);
        titleBox.setPadding(false);
        titleBox.setSpacing(false);

        Span badge = new Span(Math.round(result.confidence() * 100) + "%");
        badge.getStyle()
                .set("background", ACCENT)
                .set("color", "#FFFFFF")
                .set("border-radius", "12px")
                .set("padding", "4px 12px")
                .set("font-size", "0.9rem")
                .set("font-weight", "700");

        HorizontalLayout header = new HorizontalLayout(titleBox, badge);
        header.setWidth("100%");
        header.setAlignItems(Alignment.CENTER);
        header.expand(titleBox);
        styleCard(header);
        header.getStyle().set("border-left", "4px solid " + ACCENT);
        return header;
    }

    /** Left panel: radar chart with metric chips around the perimeter. */
    private static Component buildRadarCard(AnalysisResult result) {
        List<String> labels = List.of(
                "Социальный вектор",
                "Вектор изменений",
                "Понятийный вектор",
                "Драйв",
                "Целостность",
                "Мощь");
        List<Double> values = radarValues(result);

        RadarChart chart = new RadarChart();
        chart.setChartData(labels, values);
        chart.getStyle().set("width", "100%").set("height", "100%");

        Div grid = new Div();
        grid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "minmax(150px, 1fr) 340px minmax(150px, 1fr)")
                .set("grid-template-rows", "auto 300px auto")
                .set("gap", "10px")
                .set("align-items", "center")
                .set("width", "100%");

        // top perimeter: three axis chips
        place(grid, metricChip("X", labels.get(0), values.get(0)), 1, 1);
        place(grid, metricChip("Y", labels.get(1), values.get(1)), 2, 1);
        place(grid, metricChip("Z", labels.get(2), values.get(2)), 3, 1);
        // sides: integrity (left), drive (right)
        place(grid, metricChip("⚖", labels.get(4), values.get(4)), 1, 2);
        place(grid, metricChip("⚡", labels.get(3), values.get(3)), 3, 2);
        // bottom: power
        place(grid, metricChip("★", labels.get(5), values.get(5)), 2, 3);
        // center: chart
        chart.getStyle().set("grid-column", "2").set("grid-row", "2");
        grid.add(chart);

        Span caption = new Span("ПРОФИЛЬ");
        caption.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.08em")
                .set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout card = new VerticalLayout(caption, grid);
        styleCard(card);
        card.setMinWidth("540px");
        return card;
    }

    /** Right panel: coordinates, drive & trajectory, shadow, symbols. */
    private static Component buildSidePanel(AnalysisResult result) {
        VerticalLayout panel = new VerticalLayout(
                coordinatesCard(result),
                driveCard(result),
                shadowCard(result),
                symbolsCard(result));
        panel.setPadding(false);
        panel.setSpacing(true);
        panel.setWidth("100%");
        return panel;
    }

    private static VerticalLayout coordinatesCard(AnalysisResult result) {
        Vector3D pos = result.position();

        VerticalLayout rows = new VerticalLayout(
                axisRow(Axis.X, pos.x()),
                axisRow(Axis.Y, pos.y()),
                axisRow(Axis.Z, pos.z()));
        rows.setPadding(false);
        rows.setSpacing(true);

        VerticalLayout card = new VerticalLayout(sectionTitle("КООРДИНАТЫ И ОСИ"), rows);
        styleCard(card);
        return card;
    }

    /**
     * Creates an axis row for a coordinate in {@code [-1, 1]}, mapping it to a
     * {@code [0, 1]} progress bar and labeling zero with the positive pole.
     */
    private static VerticalLayout axisRow(Axis axis, double value) {
        Span name = new Span(axis.name() + " · " + axis.title());
        name.getStyle().set("font-weight", "600").set("font-size", "0.85rem");

        Span number = new Span(String.format("%+.2f", value));
        number.getStyle()
                .set("font-weight", "700")
                .set("font-size", "0.85rem")
                .set("color", "#C62828");

        HorizontalLayout top = new HorizontalLayout(name, number);
        top.setWidth("100%");
        top.setAlignItems(Alignment.CENTER);
        top.expand(name);
        top.setPadding(false);
        top.setSpacing(false);

        ProgressBar bar = new ProgressBar();
        bar.setValue((value + 1.0) / 2.0);
        bar.setWidth("100%");

        Span pole = new Span(value >= 0 ? axis.positivePole() : axis.negativePole());
        pole.getStyle().set("font-size", "0.75rem").set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout row = new VerticalLayout(top, bar, pole);
        row.setPadding(false);
        row.setSpacing(false);
        return row;
    }

    private static VerticalLayout driveCard(AnalysisResult result) {
        Span driveLabel = mutedLabel("ВЕДУЩИЙ МОТИВ");
        Span driveName = new Span(result.drive().label());
        driveName.getStyle().set("font-weight", "700").set("font-size", "0.9rem").set("display", "block");
        Span driveText = new Span(result.drive().description());
        driveText.getStyle().set("font-size", "0.8rem").set("color", "var(--lumo-secondary-text-color)");

        Span trajLabel = mutedLabel("НАРРАТИВНАЯ ТРАЕКТОРИЯ");
        Span trajName = new Span(result.trajectory().getTitle());
        trajName.getStyle().set("font-weight", "700").set("font-size", "0.9rem");

        VerticalLayout card = new VerticalLayout(
                sectionTitle("ДРАЙВ И ТРАЕКТОРИЯ"),
                driveLabel, driveName, driveText,
                trajLabel, trajName);
        styleCard(card);
        return card;
    }

    private static VerticalLayout shadowCard(AnalysisResult result) {
        Span octantLabel = mutedLabel("ОКТАНТ");
        Span octantText = new Span(result.shadowManifestation());

        Span archetypeLabel = mutedLabel("АРХЕТИП");
        Span archetypeText = new Span(result.dominant().getShadow().description());

        for (Span text : List.of(octantText, archetypeText)) {
            text.getStyle()
                    .set("font-size", "0.8rem")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("display", "block");
        }

        VerticalLayout card = new VerticalLayout(
                sectionTitle("ТЕНЕВЫЕ ПРОЯВЛЕНИЯ"),
                octantLabel, octantText,
                archetypeLabel, archetypeText);
        styleCard(card);
        return card;
    }

    /** Creates the symbol card, displaying a dash when the symbol list is null or empty. */
    private static VerticalLayout symbolsCard(AnalysisResult result) {
        VerticalLayout list = new VerticalLayout();
        list.setPadding(false);
        list.setSpacing(true);

        List<Symbol> symbols = result.symbols();
        if (symbols == null || symbols.isEmpty()) {
            list.add(new Span("—"));
        } else {
            for (Symbol symbol : symbols) {
                list.add(symbolRow(symbol));
            }
        }

        VerticalLayout card = new VerticalLayout(sectionTitle("АРХЕТИПИЧЕСКИЕ СИМВОЛЫ"), list);
        styleCard(card);
        return card;
    }

    private static Div symbolRow(Symbol symbol) {
        Span icon = new Span("✦");
        icon.getStyle()
                .set("color", ACCENT)
                .set("font-size", "0.9rem")
                .set("line-height", "1.2");

        Span name = new Span(symbol.name());
        name.getStyle().set("font-weight", "700").set("font-size", "0.85rem").set("display", "block");

        Span meaning = new Span(symbol.meaning());
        meaning.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("display", "block");

        Div text = new Div(name, meaning);
        Div row = new Div(icon, text);
        row.getStyle().set("display", "flex").set("gap", "8px").set("align-items", "flex-start");
        return row;
    }

    /**
     * Returns radar metrics in order: rescaled X, Y, Z; confidence; integrity;
     * and power. Axes map from {@code [-1, 1]} to {@code [0, 1]}; integrity is
     * one minus the spread of absolute coordinates, and power is their mean.
     * Each metric is clamped to {@code [0, 1]}, with NaN preserved.
     */
    private static List<Double> radarValues(AnalysisResult result) {
        Vector3D pos = result.position();
        double ax = Math.abs(pos.x());
        double ay = Math.abs(pos.y());
        double az = Math.abs(pos.z());

        double maxAbs = Math.max(ax, Math.max(ay, az));
        double minAbs = Math.min(ax, Math.min(ay, az));

        // how evenly the three axes are developed: 1 = perfectly balanced
        double integrity = clamp01(1.0 - (maxAbs - minAbs));
        // overall profile strength
        double power = clamp01((ax + ay + az) / 3.0);

        return List.of(
                clamp01((pos.x() + 1.0) / 2.0),
                clamp01((pos.y() + 1.0) / 2.0),
                clamp01((pos.z() + 1.0) / 2.0),
                clamp01(result.confidence()),
                integrity,
                power);
    }

    /** Creates a metric chip showing the value as a rounded percentage, with 1 representing 100%. */
    private static Div metricChip(String icon, String label, double value) {
        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("width", "26px")
                .set("height", "26px")
                .set("flex-shrink", "0")
                .set("border-radius", "6px")
                .set("background", "rgba(229, 115, 115, 0.18)")
                .set("color", "#C62828")
                .set("font-weight", "700")
                .set("font-size", "0.8rem");

        Span labelText = new Span(label);
        labelText.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("display", "block")
                .set("white-space", "nowrap");

        Span valueText = new Span(Math.round(value * 100) + "%");
        valueText.getStyle()
                .set("font-size", "0.9rem")
                .set("font-weight", "700")
                .set("display", "block");

        Div text = new Div(labelText, valueText);
        Div chip = new Div(iconSpan, text);
        chip.getStyle()
                .set("display", "flex")
                .set("gap", "8px")
                .set("align-items", "center")
                .set("padding", "6px 10px")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("background", "var(--lumo-base-color)");
        return chip;
    }

    /** Adds the chip to the grid at the supplied one-based CSS column and row. */
    private static void place(Div grid, Div chip, int column, int row) {
        chip.getStyle()
                .set("grid-column", String.valueOf(column))
                .set("grid-row", String.valueOf(row));
        grid.add(chip);
    }

    private static Span footer(AnalysisResult result) {
        Span span = new Span("Объект " + result.objectId());
        span.getStyle().set("font-size", "0.75rem").set("color", "var(--lumo-secondary-text-color)");
        return span;
    }

    private static Span sectionTitle(String text) {
        Span span = new Span(text);
        span.getStyle()
                .set("font-size", "0.7rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.08em")
                .set("color", "var(--lumo-secondary-text-color)");
        return span;
    }

    private static Span mutedLabel(String text) {
        Span span = new Span(text);
        span.getStyle()
                .set("font-size", "0.68rem")
                .set("font-weight", "700")
                .set("letter-spacing", "0.06em")
                .set("color", "var(--lumo-tertiary-text-color)")
                .set("display", "block");
        return span;
    }

    private static void styleCard(VerticalLayout card) {
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px");
    }

    private static void styleCard(HorizontalLayout card) {
        card.setPadding(true);
        card.setSpacing(true);
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px");
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
