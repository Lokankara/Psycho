package com.unconscious.collective.quiz.domain.history;

import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ExecutionTrace(
        String sessionId,
        String payloadHash,
        Map<String, String> answers,
        Map<Axis, Integer> axisCounts,
        Map<Axis, Double> axisSums,
        double x,
        double y,
        double z,
        String octant,
        Instant timestamp) {

    public static ExecutionTrace of(AssessmentPayload payload, SemanticProfile profile) {
        return new ExecutionTrace(
                payload.sessionId(),
                hash(payload),
                answerMap(payload),
                Map.copyOf(profile.answeredCounts()),
                Map.copyOf(profile.rawScores()),
                profile.coordinates().x(),
                profile.coordinates().y(),
                profile.coordinates().z(),
                profile.octant().name(),
                Instant.now());
    }

    private static Map<String, String> answerMap(AssessmentPayload payload) {
        Map<String, String> answers = new LinkedHashMap<>();
        for (QuizAnswer answer : payload.answers()) {
            answers.put(answer.questionId(), answer.chosenPole() == null ? "null" : answer.chosenPole().name());
        }
        return answers;
    }

    private static String hash(AssessmentPayload payload) {
        List<QuizAnswer> sorted = payload.answers().stream()
                .sorted((left, right) -> left.questionId().compareTo(right.questionId()))
                .toList();
        StringBuilder canonical = new StringBuilder();
        for (QuizAnswer answer : sorted) {
            canonical.append(answer.questionId())
                    .append('=')
                    .append(answer.chosenPole() == null ? "null" : answer.chosenPole().name())
                    .append(';');
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
