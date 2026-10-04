package com.unconscious.collective.agile.model.dto;

import java.util.List;

public record BoardResponse(
        List<ProgramIncrementOption> programIncrements,
        List<SprintOption> sprints,
        Long activeProgramIncrementId,
        Long activeSprintId,
        List<ColumnResponse> columns) {
}
