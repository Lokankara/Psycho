package com.unconscious.collective.quiz.service;

import com.unconscious.collective.quiz.domain.value.Coordinates;
import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Turns a set of forced-choice answers into {@link Coordinates} and resolves the
 * resulting {@link Octant}.
 *
 * <p>Each answered item contributes its chosen pole's sign ({@code &plusmn;1}) to the
 * item's axis. The axis coordinate is the mean of those signs, thus always within
 * {@code [-1, 1]}. Axes with no answered item resolve to {@code 0} (and, per
 * {@link Pole#ofSign(double)}, count as the positive pole).</p>
 */
@Service
public class ArchetypeScoringService {

    /**
     * Scores the supplied questions by averaging answered pole signs on each axis.
     * Missing or null choices and answer IDs absent from the question list are ignored.
     * Unanswered axes score zero; zero selects the positive side of an octant.
     *
     * @return a profile with a new subject ID, raw sums, answer counts, and no matches
     */
    public SemanticProfile score(List<BipolarQuestion> questions, Map<String, Pole> answers) {
        EnumMap<Axis, Double> rawScores = new EnumMap<>(Axis.class);
        EnumMap<Axis, Integer> counts = new EnumMap<>(Axis.class);
        for (Axis axis : Axis.values()) {
            rawScores.put(axis, 0.0);
            counts.put(axis, 0);
        }

        for (BipolarQuestion question : questions) {
            Pole chosen = answers.get(question.id());
            if (chosen == null) {
                continue;
            }
            rawScores.merge(question.axis(), (double) chosen.sign(), Double::sum);
            counts.merge(question.axis(), 1, Integer::sum);
        }

        Coordinates coordinates = Coordinates.of(
                normalize(rawScores.get(Axis.X), counts.get(Axis.X)),
                normalize(rawScores.get(Axis.Y), counts.get(Axis.Y)),
                normalize(rawScores.get(Axis.Z), counts.get(Axis.Z)));

        return new SemanticProfile(
                UUID.randomUUID().toString(),
                coordinates,
                Map.copyOf(rawScores),
                Map.copyOf(counts),
                Octant.from(coordinates),
                List.of());
    }

    /** Returns the mean pole sign, or zero when no answers were counted. */
    private static double normalize(double rawSum, int count) {
        return count == 0 ? 0.0 : rawSum / count;
    }
}
