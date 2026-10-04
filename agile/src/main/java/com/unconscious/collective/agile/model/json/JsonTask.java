package com.unconscious.collective.agile.model.json;

public record JsonTask(
        String code,
        String title,
        String bddStory,
        String executionStatus,
        String userStoryCode,
        String kanbanColumnCode
) {
}
