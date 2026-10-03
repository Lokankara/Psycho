package com.unconscious.collective.quiz.ui;

import com.unconscious.collective.quiz.domain.value.Axis;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Коллективное бессознательное — тест архетипов")
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);
        getStyle().set("text-align", "center");

        H1 title = new H1("Коллективное бессознательное");
        H3 subtitle = new H3("Трёхмерная система смысловых координат (X, Y, Z)");

        Paragraph intro = new Paragraph(
                "Ответьте на " + 18 + " пар утверждений, выбирая то, что вам ближе. "
                        + "На основе ваших ответов система построит точку в смысловом пространстве "
                        + "и определит доминирующий архетип.");
        intro.getStyle().set("max-width", "620px");

        HorizontalLayout axes = new HorizontalLayout(
                axisCard(Axis.X), axisCard(Axis.Y), axisCard(Axis.Z));
        axes.getStyle().set("flex-wrap", "wrap").set("justify-content", "center").set("gap", "1rem");

        Button start = new Button("Начать тест", e -> UI.getCurrent().navigate(QuizView.class));
        start.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button history = new Button("История результатов", e -> UI.getCurrent().navigate(HistoryView.class));

        HorizontalLayout actions = new HorizontalLayout(start, history);
        actions.setJustifyContentMode(JustifyContentMode.CENTER);

        add(title, subtitle, intro, axes, actions);
    }

    private static Div axisCard(Axis axis) {
        Div card = new Div();
        card.getStyle()
                .set("border", "1px solid #d0d0d0")
                .set("border-radius", "12px")
                .set("padding", "1rem")
                .set("width", "240px")
                .set("text-align", "left")
                .set("background", "#fafafa");

        Span heading = new Span("Ось " + axis.name() + " — " + axis.title());
        heading.getStyle().set("font-weight", "600").set("display", "block").set("margin-bottom", ".5rem");

        Span poles = new Span(axis.negativePole() + "  ⟷  " + axis.positivePole());
        poles.getStyle().set("color", "#555");

        card.add(heading, poles);
        return card;
    }
}
