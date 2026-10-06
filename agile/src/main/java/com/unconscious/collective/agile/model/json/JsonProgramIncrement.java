package com.unconscious.collective.agile.model.json;

import java.util.List;

public record JsonProgramIncrement(
        String code,
        String name,
        String goal,
        String startDate,
        String endDate,
        List<JsonSprint> sprints,
        List<JsonEpic> epics
) {
}
