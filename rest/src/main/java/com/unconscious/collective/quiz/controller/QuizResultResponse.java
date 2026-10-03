package com.unconscious.collective.quiz.controller;

import com.unconscious.collective.quiz.domain.dto.AnalysisResult;
import com.unconscious.collective.quiz.domain.symbol.Symbol;
import com.unconscious.collective.quiz.domain.value.Vector3D;

import java.util.List;

public record QuizResultResponse(
        String objectId,
        double x,
        double y,
        double z,
        String coordinateLabel,
        String octant,
        String octantName,
        String archetype,
        String meaning,
        String dominantArchetypes,
        String narrativeTrajectory,
        String shadow,
        String drive,
        String driveLabel,
        String driveDescription,
        double confidence,
        List<SymbolView> symbols) {

    public record SymbolView(String name, String meaning, String category) {
    }

    public static QuizResultResponse from(AnalysisResult result) {
        Vector3D position = result.position();
        return new QuizResultResponse(
                result.objectId(),
                position.x(),
                position.y(),
                position.z(),
                result.octant().coordinateLabel(),
                result.octant().name(),
                result.octant().octantName(),
                result.dominant().getTitle(),
                result.octant().meaning(),
                result.octant().dominantArchetypes(),
                result.octant().narrativeTrajectory(),
                result.dominant().getShadow().description(),
                result.drive().name(),
                result.drive().label(),
                result.drive().description(),
                result.confidence(),
                result.symbols().stream().map(QuizResultResponse::toSymbolView).toList());
    }

    private static SymbolView toSymbolView(Symbol symbol) {
        return new SymbolView(symbol.name(), symbol.meaning(), symbol.categoryType().title());
    }
}
