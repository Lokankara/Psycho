package com.unconscious.collective.quiz.domain.archetype;

import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Coordinates;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The outcome of a scoring pass: the resulting coordinates, the raw per-axis
 * scores, how many items were answered per axis, and the resolved octant.
 */
public record SemanticProfile(
        String subjectId,
        Coordinates coordinates,
        Map<Axis, Double> rawScores,
        Map<Axis, Integer> answeredCounts,
        Octant octant,
        List<ArchetypeMatch> matches
) implements Serializable {

    public ArchetypeMatch getPrimaryMatch() {
        return matches.stream()
                .filter(m -> m.octant() == this.octant)
                .findFirst()
                .orElse(matches.isEmpty() ? null : matches.get(0));
    }

    public List<ArchetypeMatch> getMatchesByConfidence(double threshold) {
        return matches.stream()
                .filter(m -> m.confidence() >= threshold)
                .toList();
    }
}
