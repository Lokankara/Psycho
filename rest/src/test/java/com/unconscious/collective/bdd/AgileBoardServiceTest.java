package com.unconscious.collective.bdd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({AgileBoardService.class, AgileSeeder.class})
class AgileBoardServiceTest {

    private static final List<String> EXPECTED_COLUMNS =
            List.of("To Do", "In Progress", "Code Review", "Done");

    @Autowired
    private AgileSeeder seeder;

    @Autowired
    private AgileBoardService service;

    @Autowired
    private ProgramIncrementRepository programIncrementRepository;

    @Autowired
    private SprintRepository sprintRepository;

    @Autowired
    private KanbanColumnRepository kanbanColumnRepository;

    @Autowired
    private UserStoryRepository userStoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void seedData() {
        seeder.run(null);
    }

    @Test
    void seederCreatesPi1WithThreeSprintsAndFourColumns() {
        assertEquals(1, programIncrementRepository.count());
        assertEquals("PI-1", programIncrementRepository.findAll().getFirst().getCode());
        assertEquals(3, sprintRepository.count());
        assertEquals(4, kanbanColumnRepository.count());
        assertFalse(userStoryRepository.findAll().isEmpty(), "Seeder must create sample stories");
    }

    @Test
    void seederIsIdempotentWhenRunTwice() {
        long columnsBefore = kanbanColumnRepository.count();
        long storiesBefore = userStoryRepository.count();
        seeder.run(null);
        assertEquals(columnsBefore, kanbanColumnRepository.count());
        assertEquals(storiesBefore, userStoryRepository.count());
    }

    @Test
    void boardReturnsOrderedColumnsGroupedStoriesAndTasks() {
        AgileDtos.BoardDto board = service.board(null, null);

        assertNotNull(board.programIncrement());
        assertEquals("PI-1", board.programIncrement().code());
        assertEquals(EXPECTED_COLUMNS, board.columns().stream().map(AgileDtos.ColumnDto::name).toList());
        assertEquals(3, board.sprints().size());
        assertFalse(board.stories().isEmpty());
        for (AgileDtos.StoryDto story : board.stories()) {
            assertTrue(board.columns().stream().anyMatch(c -> c.id().equals(story.columnId())),
                    "Story must belong to a board column");
            assertFalse(story.tasks().isEmpty(), "Sample stories must carry tasks");
        }
    }

    @Test
    void boardFiltersStoriesBySelectedSprint() {
        Long sprintId = sprintRepository.findAll().getFirst().getId();

        AgileDtos.BoardDto board = service.board(null, sprintId);

        assertFalse(board.stories().isEmpty());
        for (AgileDtos.StoryDto story : board.stories()) {
            assertEquals(sprintId, story.sprintId());
        }
    }

    @Test
    void movingStoryToAnotherColumnPersists() {
        AgileDtos.StoryDto story = service.board(null, null).stories().getFirst();
        Long doneId = kanbanColumnRepository.findAll().stream()
                .filter(c -> c.getName().equals("Done"))
                .findFirst()
                .orElseThrow()
                .getId();

        AgileDtos.StoryDto moved = service.moveStory(story.id(), doneId);

        assertEquals(doneId, moved.columnId());
        assertEquals(doneId, userStoryRepository.findById(story.id()).orElseThrow().getColumn().getId());
    }

    @Test
    void movingTaskToAnotherColumnPersists() {
        Task task = taskRepository.findAll().getFirst();
        Long reviewId = kanbanColumnRepository.findAll().stream()
                .filter(c -> c.getName().equals("Code Review"))
                .findFirst()
                .orElseThrow()
                .getId();

        AgileDtos.TaskDto moved = service.moveTask(task.getId(), reviewId);

        assertEquals(reviewId, moved.columnId());
        assertEquals(reviewId, taskRepository.findById(task.getId()).orElseThrow().getColumn().getId());
    }

    @Test
    void recordingBddExecutionUpdatesStoryStatusAndTimestamp() {
        UserStory story = userStoryRepository.findAll().stream()
                .filter(s -> s.getBddStory() != null && !s.getBddStory().isBlank())
                .findFirst()
                .orElseThrow();

        AgileDtos.StoryDto updated = service.recordBddExecution(story.getBddStory(), "PASSED");

        assertEquals("PASSED", updated.executionStatus());
        assertNotNull(updated.lastRunAt());
        UserStory reloaded = userStoryRepository.findById(story.getId()).orElseThrow();
        assertEquals(UserStory.ExecutionStatus.PASSED, reloaded.getExecutionStatus());
        assertNotNull(reloaded.getLastRunAt());
    }

    @Test
    void recordingUnknownBddStoryFails() {
        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> service.recordBddExecution("does_not_exist.story", "PASSED"));
    }
}