package com.unconscious.collective.agile.model.json;

public record JsonUserStory(
        String code,
        String title,
        int storyPoints,
        String priority,
        String bddStory,
        String acceptanceCriteria,
        String executionStatus,
        String epicCode,
        String sprintCode,
        String kanbanColumnCode
) {
}
