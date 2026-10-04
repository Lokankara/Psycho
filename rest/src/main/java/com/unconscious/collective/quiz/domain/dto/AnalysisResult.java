package com.unconscious.collective.quiz.domain.dto;

import com.unconscious.collective.quiz.domain.archetype.Archetype;
import com.unconscious.collective.quiz.domain.archetype.CoreDriveType;
import com.unconscious.collective.quiz.domain.archetype.NarrativeArchetype;
import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.symbol.Symbol;
import com.unconscious.collective.quiz.domain.value.Vector3D;

import java.util.List;

public record AnalysisResult(
        String objectId,
        Vector3D position,
        Octant octant,
        Archetype dominant,
        NarrativeArchetype trajectory,
        String shadowManifestation,
        CoreDriveType drive,
        double confidence,
        List<Symbol> symbols) {
}
