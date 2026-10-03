package com.unconscious.collective.quiz.domain.dto;

import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record AssessmentPayload(String sessionId, List<QuizAnswer> answers) {

    public AssessmentPayload {
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        answers = List.copyOf(Objects.requireNonNull(answers, "answers must not be null"));
    }

    public List<Integer> answersFor(Axis axis, Map<String, Axis> questionAxisMap) {
        return answers.stream()
                .filter(answer -> questionAxisMap.get(answer.questionId()) == axis)
                .map(answer -> answer.chosenPole().sign())
                .toList();
    }
}
