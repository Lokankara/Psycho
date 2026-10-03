package com.unconscious.collective.bdd.dto;

import java.util.List;

public final class AgileDtos {

    private AgileDtos() {
    }

    public record ProgramIncrementOption(Long id, String code, String name, String goal) {
    }

    public record SprintOption(Long id, String code, String name, String goal,
                               String startDate, String endDate) {
    }

    public record StoryCard(Long id, String code, String title, Integer storyPoints,
                            String priority, Long sprintId, Long epicId,
                            String acceptanceCriteria, String bddStory,
                            String executionStatus, String lastRunAt) {
    }

    public record TaskCard(Long id, String code, String title, Long storyId,
                           String bddStory, String executionStatus, String lastRunAt) {
    }

    public record ColumnResponse(String code, String name, int position,
                                 List<StoryCard> stories, List<TaskCard> tasks) {
    }

    public record BoardResponse(List<ProgramIncrementOption> programIncrements,
                                List<SprintOption> sprints,
                                Long activeProgramIncrementId,
                                Long activeSprintId,
                                List<ColumnResponse> columns) {
    }

    public record MoveRequest(String columnCode) {
    }

    public record BddExecutionRequest(String bddStory, String status) {
    }

    public record BddExecutionResponse(int storiesUpdated, int tasksUpdated) {
    }
}
