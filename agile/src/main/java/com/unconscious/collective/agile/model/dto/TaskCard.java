package com.unconscious.collective.agile.model.dto;

public record TaskCard(Long id, String code, String title, Long storyId,
                       String bddStory, String executionStatus, String lastRunAt) {
}
