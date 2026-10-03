package com.unconscious.collective.quiz.domain.quiz;

import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-user (Vaadin session scoped) quiz progress: the collected answers, the
 * current step index, and the last computed profile.
 */
@Getter
@Component
@VaadinSessionScope
public class QuizSession implements Serializable {

    private final Map<String, Pole> answers = new LinkedHashMap<>();
    private int index;
    @Setter
    private SemanticProfile lastProfile;

    /** Stores the choice for a question, replacing any previous choice for that ID. */
    public void answer(String questionId, Pole pole) {
        answers.put(questionId, pole);
    }

    /** Returns the stored choice, or null if the question is unanswered or its stored choice is null. */
    public Pole answerOf(String questionId) {
        return answers.get(questionId);
    }

    /** Advances the question index without checking the question bank size. */
    public void next() {
        index++;
    }

    /** Moves back one question, leaving the index at zero when already at the start. */
    public void previous() {
        if (index > 0) {
            index--;
        }
    }

    /** Clears all progress, starting a fresh run. */
    public void reset() {
        answers.clear();
        index = 0;
        lastProfile = null;
    }
}
