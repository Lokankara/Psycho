package com.unconscious.collective.agile.service;

import com.unconscious.collective.agile.model.dto.BddExecutionResponse;
import com.unconscious.collective.agile.model.dto.BoardResponse;
import com.unconscious.collective.agile.model.dto.MoveRequest;
import com.unconscious.collective.agile.model.dto.StoryCard;
import com.unconscious.collective.agile.model.dto.TaskCard;

import java.util.Optional;

public interface IAgileBoardOperations {

    BoardResponse getBoard(Long programIncrementId, Long sprintId);

    Optional<StoryCard> moveStory(Long storyId, MoveRequest request);

    Optional<TaskCard> moveTask(Long taskId, MoveRequest request);

    BddExecutionResponse recordExecution(String bddStory, String statusRaw);
}
