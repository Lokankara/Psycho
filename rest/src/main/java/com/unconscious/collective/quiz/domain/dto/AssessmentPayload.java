package com.unconscious.collective.quiz.domain.dto;

import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AssessmentPayload(String sessionId, List<QuizAnswer> answers) {

    /**
     * Creates a payload with an unmodifiable snapshot of the answer list.
     *
     * @throws NullPointerException if the session ID, answer list, or any list element is null
     */
    public AssessmentPayload {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        answers = List.copyOf(Objects.requireNonNull(answers, "answers must not be null"));
    }

    /**
     * Returns pole signs ({@code -1} or {@code 1}) for answers mapped to the requested
     * axis, preserving answer order in an unmodifiable list. A null axis also matches
     * question IDs with no mapping or a null mapping.
     *
     * @throws NullPointerException if the map is null and answers are present, or a
     *         selected answer has a null pole
     */
    public List<Integer> answersFor(Axis axis, Map<String, Axis> questionAxisMap) {
        return answers.stream()
                .filter(answer -> questionAxisMap.get(answer.questionId()) == axis)
                .map(answer -> answer.chosenPole().sign())
                .toList();
    }
}
