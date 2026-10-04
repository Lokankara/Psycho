package com.unconscious.collective.quiz.service;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Coordinates;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Turns a set of forced-choice answers into {@link Coordinates} and resolves the
 * resulting {@link Octant}.
 *
 * <p>Each axis coordinate is the mean of the pole signs ({@code &plusmn;1}) of the
 * questions answered for that axis, so it always lies in {@code [-1, 1]}. An axis with
 * no answered question resolves to {@code 0}. Every coordinate is rounded to two
 * decimal places so that identical answer sets always yield identical coordinates and
 * therefore an identical {@link Octant}.</p>
 *
 * <p>Instances hold no calculation state: every accumulator is local to
 * {@link #score(List, AssessmentPayload)}, so concurrent runs cannot leak into each
 * other.</p>
 */
@Service
public class ArchetypeScoringService {

    /**
     * Scores the supplied payload by averaging answered pole signs on each axis.
     * Missing or null choices and answer IDs absent from the question list are ignored.
     * Unanswered axes score zero; zero selects the positive side of an octant.
     *
     * @return a profile with a new subject ID, raw sums, answer counts, and no matches
     */
    public SemanticProfile score(List<BipolarQuestion> questions, AssessmentPayload payload) {
        Map<String, Axis> questionAxisMap = new HashMap<>();
        for (BipolarQuestion question : questions) {
            questionAxisMap.put(question.id(), question.axis());
        }

        EnumMap<Axis, Double> rawScores = new EnumMap<>(Axis.class);
        EnumMap<Axis, Integer> counts = new EnumMap<>(Axis.class);
        EnumMap<Axis, Double> values = new EnumMap<>(Axis.class);
        for (Axis axis : Axis.values()) {
            List<Integer> signs = payload.answersFor(axis, questionAxisMap);
            double sum = signs.stream().mapToInt(Integer::intValue).sum();
            int count = signs.size();
            rawScores.put(axis, sum);
            counts.put(axis, count);
            values.put(axis, normalize(sum, count));
        }

        Coordinates coordinates = Coordinates.of(
                values.get(Axis.X), values.get(Axis.Y), values.get(Axis.Z));

        return new SemanticProfile(
                UUID.randomUUID().toString(),
                coordinates,
                Map.copyOf(rawScores),
                Map.copyOf(counts),
                Octant.from(coordinates),
                List.of());
    }

    /**
     * Returns {@code sum / count} rounded to two decimal places, or {@code 0.0} when
     * {@code count == 0}.
     */
    private static double normalize(double sum, int count) {
        if (count == 0) {
            return 0.0;
        }
        double mean = sum / count;
        return Math.round(mean * 100.0) / 100.0;
    }
}

