package com.unconscious.collective.quiz.ui;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.archetype.CoreDriveType;
import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.symbol.Symbol;
import com.unconscious.collective.quiz.service.SemanticMatchingService;
import com.unconscious.collective.quiz.domain.quiz.QuizSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("result")
@PageTitle("Ваш архетип")
public class ResultView extends VerticalLayout {

    /**
     * Displays the session's latest profile and its match, or a prompt to take the
     * quiz when no profile is available. The retake action clears session progress.
     */
    public ResultView(QuizSession session, SemanticMatchingService matchingService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);
        getStyle().set("text-align", "center");

        SemanticProfile profile = session.getLastProfile();
        if (profile == null) {
            add(new H3("Результат недоступен"),
                    new Span("Сначала пройдите тест."),
                    primary("Пройти тест", () -> UI.getCurrent().navigate(QuizView.class)));
            return;
        }

        Octant octant = profile.octant();
        ArchetypeMatch match = matchingService.match(profile);
        CoreDriveType coreDrive = match.coreDrive();

        H1 archetype = new H1(match.archetype().getTitle());
        H3 octantName = new H3(octant.octantName());

        Span coords = new Span("Октант " + octant.octantName() + " · координаты "
                + formatCoordinates(profile));
        coords.getStyle().set("color", "#666");

        VerticalLayout axisBars = new VerticalLayout(
                axisBar(Axis.X, profile.coordinates().value(Axis.X)),
                axisBar(Axis.Y, profile.coordinates().value(Axis.Y)),
                axisBar(Axis.Z, profile.coordinates().value(Axis.Z)));
        axisBars.setPadding(false);
        axisBars.setSpacing(false);
        axisBars.setAlignItems(Alignment.CENTER);
        axisBars.getStyle().set("gap", "0.5rem");

        Div info = new Div(
                infoRow("Смысловое содержание", octant.meaning()),
                infoRow("Доминирующие архетипы", octant.dominantArchetypes()),
                infoRow("Нарративная траектория", octant.narrativeTrajectory()),
                infoRow("Тень", match.archetype().getShadow().description()));
        styleCard(info);

        Div driveCard = new Div(
                infoRow("Ведущий мотив", coreDrive.label()),
                infoRow("Описание мотива", coreDrive.description()),
                confidenceRow(match.confidence()));
        styleCard(driveCard);

        Div symbolsCard = new Div(
                symbolsRow("Архетипические символы", match.symbols()));
        styleCard(symbolsCard);

        HorizontalLayout actions = new HorizontalLayout(
                primary("Пройти заново", () -> {
                    session.reset();
                    UI.getCurrent().navigate(QuizView.class);
                }),
                new Button("История", e -> UI.getCurrent().navigate(HistoryView.class)));

        add(archetype, octantName, coords, axisBars, info, driveCard, symbolsCard, actions);
    }

    /** Creates a labeled bar mapping a coordinate from {@code [-1, 1]} to {@code [0, 1]}. */
    private static Component axisBar(Axis axis, double value) {
        Span title = new Span("Ось " + axis.name() + " — " + axis.title());
        title.getStyle().set("font-weight", "600");

        Span negative = new Span(axis.negativePole());
        negative.getStyle().set("color", "#888");
        Span positive = new Span(axis.positivePole());
        positive.getStyle().set("color", "#888");

        ProgressBar bar = new ProgressBar();
        bar.setValue((value + 1.0) / 2.0);
        bar.setWidth("300px");

        Span numeric = new Span(String.format("%+.2f", value));
        numeric.getStyle().set("font-weight", "600").set("min-width", "3.5rem");

        HorizontalLayout row = new HorizontalLayout(negative, bar, positive, numeric);
        row.setAlignItems(Alignment.CENTER);
        row.setJustifyContentMode(JustifyContentMode.CENTER);

        VerticalLayout block = new VerticalLayout(title, row);
        block.setPadding(false);
        block.setSpacing(false);
        block.setAlignItems(Alignment.CENTER);
        return block;
    }

    private static Div infoRow(String label, String value) {
        Div row = new Div();
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "600").set("display", "block");
        row.add(labelSpan, new Span(value));
        row.getStyle().set("margin-bottom", "0.75rem");
        return row;
    }

    /** Formats X, Y, and Z with explicit signs and two decimal places using the default locale. */
    private static String formatCoordinates(SemanticProfile profile) {
        return String.format("(%+.2f, %+.2f, %+.2f)",
                profile.coordinates().x(), profile.coordinates().y(), profile.coordinates().z());
    }

    private static void styleCard(Div card) {
        card.getStyle()
                .set("text-align", "left")
                .set("max-width", "620px")
                .set("border", "1px solid #d0d0d0")
                .set("border-radius", "12px")
                .set("padding", "1rem 1.25rem")
                .set("background", "#fafafa");
    }

    /** Creates a bar for confidence in {@code [0, 1]} with a rounded percentage label. */
    private static Div confidenceRow(double confidence) {
        Div row = new Div();
        Span label = new Span("Уверенность");
        label.getStyle().set("font-weight", "600").set("display", "block");

        ProgressBar bar = new ProgressBar();
        bar.setValue(confidence);
        bar.setWidth("100%");

        Span numeric = new Span(Math.round(confidence * 100) + "%");
        numeric.getStyle().set("font-weight", "600");

        row.add(label, bar, numeric);
        row.getStyle().set("margin-top", "0.25rem");
        return row;
    }

    private static Div symbolsRow(String label, List<Symbol> symbols) {
        Div row = new Div();

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "600").set("display", "block").set("margin-bottom", "0.25rem");
        row.add(labelSpan);

        for (Symbol symbol : symbols) {
            Span name = new Span(symbol.name());
            name.getStyle().set("font-weight", "600");
            Span meaning = new Span(" — " + symbol.meaning());
            Div item = new Div(name, meaning);
            item.getStyle().set("margin-bottom", "0.35rem");
            row.add(item);
        }
        return row;
    }

    private static Button primary(String text, Runnable action) {
        Button button = new Button(text, e -> action.run());
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return button;
    }
}
