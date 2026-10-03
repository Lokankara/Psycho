package com.unconscious.collective.quiz.domain.dto;

import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;

import java.util.LinkedHashMap;
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
     * axis. Unanswered items (null pole) and question IDs absent from the axis map are
     * skipped, so the result counts every answered question exactly once; a repeated
     * question ID contributes its last non-null pole.
     *
     * @throws NullPointerException if the axis map is null and answers are present
     */
    public List<Integer> answersFor(Axis axis, Map<String, Axis> questionAxisMap) {
        Map<String, Pole> resolved = new LinkedHashMap<>();
        for (QuizAnswer answer : answers) {
            Pole pole = answer.chosenPole();
            if (pole == null || questionAxisMap.get(answer.questionId()) != axis) {
                continue;
            }
            resolved.put(answer.questionId(), pole);
        }
        return resolved.values().stream().map(Pole::sign).toList();
    }
}

