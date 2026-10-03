package com.unconscious.collective.quiz.ui;

import com.unconscious.collective.quiz.dao.QuizResult;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.service.QuizService;
import com.unconscious.collective.quiz.service.ResultService;
import com.unconscious.collective.quiz.domain.quiz.QuizSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Route("quiz")
@PageTitle("Тест архетипов")
public class QuizView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(QuizView.class);

    private final QuizService quizService;
    private final ResultService resultService;
    private final QuizSession session;
    private final List<BipolarQuestion> questions;

    private final ProgressBar progress = new ProgressBar();
    private final H2 counter = new H2();
    private final Span axisHint = new Span();
    private final Div negativeCard = new Div();
    private final Div positiveCard = new Div();

    private Pole selection;

    public QuizView(QuizService quizService, ResultService resultService, QuizSession session) {
        this.quizService = quizService;
        this.resultService = resultService;
        this.session = session;
        this.questions = quizService.questions();

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSpacing(true);
        setPadding(true);

        progress.setWidth("620px");
        axisHint.getStyle().set("color", "#777").set("font-size", "0.95rem");

        negativeCard.addClickListener(e -> choose(Pole.NEGATIVE));
        positiveCard.addClickListener(e -> choose(Pole.POSITIVE));

        HorizontalLayout cards = new HorizontalLayout(negativeCard, positiveCard);
        cards.setWidth("620px");
        cards.getStyle().set("gap", "1rem").set("align-items", "stretch");

        add(progress, counter, axisHint, cards);
        render();
    }

    /** Displays the current question and stored choice, or finishes when the index reaches the bank size. */
    private void render() {
        int total = questions.size();
        int index = session.getIndex();
        if (index >= total) {
            finish();
            return;
        }

        BipolarQuestion question = questions.get(index);
        counter.setText("Вопрос " + (index + 1) + " из " + total);
        progress.setValue((double) index / total);
        axisHint.setText(axisHint(question.axis()));

        selection = session.answerOf(question.id());
        negativeCard.setText(question.negativeStatement());
        positiveCard.setText(question.positiveStatement());

        styleCard(negativeCard, selection == Pole.NEGATIVE);
        styleCard(positiveCard, selection == Pole.POSITIVE);
    }

    /** Stores the current choice and immediately advances to the next question or finishes the quiz. */
    private void choose(Pole pole) {
        BipolarQuestion question = questions.get(session.getIndex());
        session.answer(question.id(), pole);
        selection = pole;
        styleCard(negativeCard, pole == Pole.NEGATIVE);
        styleCard(positiveCard, pole == Pole.POSITIVE);
        advance();
    }

    /** Finishes on the last question; otherwise increments the session index and displays the next question. */
    private void advance() {
        if (session.getIndex() >= questions.size() - 1) {
            finish();
        } else {
            session.next();
            render();
        }
    }

    /**
     * Scores the answers, stores the profile in the session, saves a history entry,
     * and navigates to the result view. Persistence failures propagate after the
     * session profile has been set and prevent navigation.
     */
    private void finish() {
        SemanticProfile profile = quizService.evaluate(session.getAnswers());
        session.setLastProfile(profile);
        QuizResult save = resultService.save(profile);
        LOG.info("QuizResult saved {}", save);
        progress.setValue(1.0);
        UI.getCurrent().navigate(ResultView.class);
    }

    private static String axisHint(Axis axis) {
        return "Ось " + axis.name() + " · " + axis.negativePole() + "  ⟷  " + axis.positivePole();
    }

    private static void styleCard(Div card, boolean selected) {
        card.getStyle()
                .set("flex", "1")
                .set("cursor", "pointer")
                .set("border-radius", "14px")
                .set("padding", "1.25rem")
                .set("min-height", "120px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("transition", "all .15s")
                .set("border", selected ? "2px solid #1a73e8" : "1px solid #d0d0d0")
                .set("background", selected ? "#e8f0fe" : "#ffffff")
                .set("box-shadow", selected ? "0 2px 10px rgba(26,115,232,.25)" : "none");
    }
}
