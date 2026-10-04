package com.unconscious.collective.agile.model.dto;

public record StoryCard(Long id, String code, String title, Integer storyPoints,
                        String priority, Long sprintId, Long epicId,
                        String acceptanceCriteria, String bddStory,
                        String executionStatus, String lastRunAt) {
}
