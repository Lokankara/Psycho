package com.unconscious.collective.quiz.controller;

import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.symbol.Symbol;

import java.util.List;

/**
 * Outgoing payload of the analysis endpoint.
 */
public record AnalysisResponse(
        double x,
        double y,
        double z,
        String coordinateLabel,
        String octant,
        String archetype,
        String coreDrive,
        List<String> symbols,
        double confidence) {

    /** Combines profile coordinates with match labels, symbol names, and confidence for the API. */
    public static AnalysisResponse from(SemanticProfile profile, ArchetypeMatch match) {
        return new AnalysisResponse(
                profile.coordinates().x(),
                profile.coordinates().y(),
                profile.coordinates().z(),
                match.octant().coordinateLabel(),
                match.octant().octantName(),
                match.archetype().getTitle(),
                match.coreDrive().label(),
                match.symbols().stream().map(Symbol::name).toList(),
                match.confidence());
    }
}
