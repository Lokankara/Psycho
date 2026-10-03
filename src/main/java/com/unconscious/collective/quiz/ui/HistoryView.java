package com.unconscious.collective.quiz.ui;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.dao.QuizResult;
import com.unconscious.collective.quiz.service.ResultService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Route("history")
@PageTitle("История результатов")
public class HistoryView extends VerticalLayout {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    public HistoryView(ResultService resultService) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setSpacing(true);
        setPadding(true);

        Grid<QuizResult> grid = new Grid<>();
        grid.addColumn(r -> FORMATTER.format(r.getCreatedAt())).setHeader("Дата").setAutoWidth(true);
        grid.addColumn(r -> Octant.valueOf(r.getOctantCode()).archetypeName())
                .setHeader("Архетип").setAutoWidth(true);
        grid.addColumn(r -> Octant.valueOf(r.getOctantCode()).octantName())
                .setHeader("Октант").setAutoWidth(true);
        grid.addColumn(r -> String.format("(%+.2f, %+.2f, %+.2f)", r.getX(), r.getY(), r.getZ()))
                .setHeader("Координаты").setAutoWidth(true);
        grid.setItems(resultService.recent());
        grid.setWidth("800px");

        Button retake = new Button("Пройти тест", e -> UI.getCurrent().navigate(QuizView.class));
        retake.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new H1("История результатов"), grid, retake);
    }
}
