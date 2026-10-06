package com.unconscious.collective.agile.model.json;

import java.util.List;

public record JsonInitData(
        List<JsonKanbanColumn> kanbanColumns,
        List<JsonProgramIncrement> programIncrements,
        List<JsonUserStory> userStories,
        List<JsonTask> tasks
) {
}
