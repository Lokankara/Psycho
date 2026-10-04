package com.unconscious.collective.agile.controller;

import com.unconscious.collective.agile.model.dto.BddExecutionRequest;
import com.unconscious.collective.agile.model.dto.BddExecutionResponse;
import com.unconscious.collective.agile.model.dto.BoardResponse;
import com.unconscious.collective.agile.model.dto.MoveRequest;
import com.unconscious.collective.agile.model.dto.StoryCard;
import com.unconscious.collective.agile.model.dto.TaskCard;
import com.unconscious.collective.agile.service.IAgileBoardOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/agile")
public class AgileController {

    private final IAgileBoardOperations boardService;

    public AgileController(IAgileBoardOperations board) {
        this.boardService = board;
    }

    @GetMapping("/board")
    public BoardResponse board(@RequestParam(required = false) Long programIncrementId,
            @RequestParam(required = false) Long sprintId) {
        return boardService.getBoard(programIncrementId, sprintId);
    }

    @PatchMapping("/stories/{id}/column")
    public ResponseEntity<StoryCard> moveStory(@PathVariable Long id, @RequestBody MoveRequest request) {
        return boardService.moveStory(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/tasks/{id}/column")
    public ResponseEntity<TaskCard> moveTask(@PathVariable Long id, @RequestBody MoveRequest request) {
        return boardService.moveTask(id, request)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/bdd-execution")
    public BddExecutionResponse recordExecution(@RequestBody BddExecutionRequest request) {
        try {
            return boardService.recordExecution(request.bddStory(), request.status());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
