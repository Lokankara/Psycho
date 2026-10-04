package com.unconscious.collective.agile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.unconscious.collective.agile.dao.KanbanColumnRepository;
import com.unconscious.collective.agile.dao.ProgramIncrementRepository;
import com.unconscious.collective.agile.dao.SprintRepository;
import com.unconscious.collective.agile.dao.TaskRepository;
import com.unconscious.collective.agile.dao.UserStoryRepository;
import com.unconscious.collective.agile.model.dto.BddExecutionResponse;
import com.unconscious.collective.agile.model.dto.BoardResponse;
import com.unconscious.collective.agile.model.dto.ColumnResponse;
import com.unconscious.collective.agile.model.dto.MoveRequest;
import com.unconscious.collective.agile.model.dto.StoryCard;
import com.unconscious.collective.agile.model.dto.TaskCard;
import com.unconscious.collective.agile.model.entity.BddStatus;
import com.unconscious.collective.agile.model.entity.Epic;
import com.unconscious.collective.agile.model.entity.KanbanColumn;
import com.unconscious.collective.agile.model.entity.ProgramIncrement;
import com.unconscious.collective.agile.model.entity.Sprint;
import com.unconscious.collective.agile.model.entity.Task;
import com.unconscious.collective.agile.model.entity.UserStory;
import com.unconscious.collective.agile.service.AgileBoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AgileBoardServiceTest {

    private static final List<String> EXPECTED_COLUMNS =
            List.of("To Do", "In Progress", "Code Review", "Done");

    @Mock
    private KanbanColumnRepository columns;

    @Mock
    private ProgramIncrementRepository programIncrements;

    @Mock
    private SprintRepository sprints;

    @Mock
    private UserStoryRepository stories;

    @Mock
    private TaskRepository tasks;

    @InjectMocks
    private AgileBoardService service;

    @BeforeEach
    void seedData() throws Exception {
        Mockito.reset(columns, programIncrements, sprints, stories, tasks);

        KanbanColumn todo = new KanbanColumn("TO_DO", "To Do", 0);
        KanbanColumn inProgress = new KanbanColumn("IN_PROGRESS", "In Progress", 1);
        KanbanColumn codeReview = new KanbanColumn("CODE_REVIEW", "Code Review", 2);
        KanbanColumn done = new KanbanColumn("DONE", "Done", 3);

        when(columns.findAllByOrderByPositionAsc()).thenReturn(List.of(todo, inProgress, codeReview, done));
        when(columns.findAllByOrderByPositionAscOrDefault()).thenReturn(List.of(todo, inProgress, codeReview, done));
        when(columns.findByCode("TO_DO")).thenReturn(Optional.of(todo));
        when(columns.findByCode("IN_PROGRESS")).thenReturn(Optional.of(inProgress));
        when(columns.findByCode("CODE_REVIEW")).thenReturn(Optional.of(codeReview));
        when(columns.findByCode("DONE")).thenReturn(Optional.of(done));

        ProgramIncrement pi = new ProgramIncrement("PI-1", "PI-1", "goal",
                LocalDate.of(2026, 10, 1), LocalDate.of(2027, 1, 31));
        setField(pi, "id", 1L);

        Sprint sprint1 = new Sprint("PI1-S1", "S1", "goal", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 14), pi);
        setField(sprint1, "id", 1L);
        Sprint sprint2 = new Sprint("PI1-S2", "S2", "goal", LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 28), pi);
        setField(sprint2, "id", 2L);
        Sprint sprint3 = new Sprint("PI1-S3", "S3", "goal", LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 11), pi);
        setField(sprint3, "id", 3L);

        when(programIncrements.findAllByOrderByStartDateAsc()).thenReturn(List.of(pi));
        when(sprints.findByProgramIncrementIdOrderByStartDateAsc(1L)).thenReturn(List.of(sprint1, sprint2, sprint3));

        Epic scoring = new Epic("EP-1", "Scoring", "desc", pi);
        setField(scoring, "id", 1L);
        Epic bdd = new Epic("EP-2", "BDD", "desc", pi);
        setField(bdd, "id", 2L);
        Epic board = new Epic("EP-3", "Board", "desc", pi);
        setField(board, "id", 3L);

        UserStory us1 = new UserStory();
        us1.setId(1L);
        us1.setCode("US-1");
        us1.setTitle("Verify coordinate determinism");
        us1.setStoryPoints(3);
        us1.setPriority("HIGH");
        us1.setAcceptanceCriteria("ac");
        us1.setBddStory("verify_coordinate_calculation.story");
        us1.setEpic(scoring);
        us1.setSprint(sprint1);
        us1.setKanbanColumn(done);
        us1.setExecutionStatus(BddStatus.PASSED);
        us1.setLastRunAt(Instant.now());

        UserStory us2 = new UserStory();
        us2.setId(2L);
        us2.setCode("US-2");
        us2.setTitle("Export execution history");
        us2.setStoryPoints(5);
        us2.setPriority("HIGH");
        us2.setAcceptanceCriteria("ac");
        us2.setBddStory(null);
        us2.setEpic(scoring);
        us2.setSprint(sprint2);
        us2.setKanbanColumn(inProgress);
        us2.setExecutionStatus(BddStatus.FAILED);
        us2.setLastRunAt(Instant.now());

        UserStory us3 = new UserStory();
        us3.setId(3L);
        us3.setCode("US-3");
        us3.setTitle("Page objects");
        us3.setStoryPoints(3);
        us3.setPriority("MEDIUM");
        us3.setAcceptanceCriteria("ac");
        us3.setBddStory(null);
        us3.setEpic(bdd);
        us3.setSprint(sprint2);
        us3.setKanbanColumn(codeReview);
        us3.setExecutionStatus(BddStatus.NOT_RUN);
        us3.setLastRunAt(null);

        UserStory us4 = new UserStory();
        us4.setId(4L);
        us4.setCode("US-4");
        us4.setTitle("Interactive kanban board");
        us4.setStoryPoints(5);
        us4.setPriority("MEDIUM");
        us4.setAcceptanceCriteria("ac");
        us4.setBddStory(null);
        us4.setEpic(board);
        us4.setSprint(sprint3);
        us4.setKanbanColumn(todo);
        us4.setExecutionStatus(BddStatus.NOT_RUN);
        us4.setLastRunAt(null);

        when(stories.findAllByOrderByCodeAsc()).thenReturn(List.of(us1, us2, us3, us4));
        when(stories.findBySprintIdOrderByCodeAsc(1L)).thenReturn(List.of(us1));
        when(stories.findBySprintIdOrderByCodeAsc(2L)).thenReturn(List.of(us2, us3));
        when(stories.findBySprintIdOrderByCodeAsc(3L)).thenReturn(List.of(us4));
        when(stories.findByBddStory(any())).thenAnswer(invocation -> {
            String bddStory = invocation.getArgument(0);
            return Stream.of(us1, us2, us3, us4)
                    .filter(story -> bddStory.equals(story.getBddStory()))
                    .toList();
        });
        when(stories.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return Stream.of(us1, us2, us3, us4)
                    .filter(s -> s.getId().equals(id))
                    .findFirst();
        });

        Task t1 = new Task();
        t1.setId(1L);
        t1.setCode("T-1");
        t1.setTitle("Round coordinates");
        t1.setUserStory(us1);
        t1.setKanbanColumn(done);
        t1.setBddStory("verify_coordinate_calculation.story");
        t1.setExecutionStatus(BddStatus.PASSED);
        t1.setLastRunAt(Instant.now());

        Task t2 = new Task();
        t2.setId(2L);
        t2.setCode("T-2");
        t2.setTitle("Write HistoryResult");
        t2.setUserStory(us2);
        t2.setKanbanColumn(inProgress);
        t2.setBddStory(null);
        t2.setExecutionStatus(BddStatus.FAILED);
        t2.setLastRunAt(Instant.now());

        Task t3 = new Task();
        t3.setId(3L);
        t3.setCode("T-3");
        t3.setTitle("Drag and drop");
        t3.setUserStory(us4);
        t3.setKanbanColumn(codeReview);
        t3.setBddStory(null);
        t3.setExecutionStatus(BddStatus.NOT_RUN);
        t3.setLastRunAt(null);

        Task t4 = new Task();
        t4.setId(4L);
        t4.setCode("T-4");
        t4.setTitle("Centralise selectors");
        t4.setUserStory(us3);
        t4.setKanbanColumn(todo);
        t4.setBddStory(null);
        t4.setExecutionStatus(BddStatus.NOT_RUN);
        t4.setLastRunAt(null);

        when(tasks.findAllByOrderByCodeAsc()).thenReturn(List.of(t1, t2, t3, t4));
        when(tasks.findByUserStorySprintIdOrderByCodeAsc(any())).thenReturn(List.of(t1, t2, t3, t4));
        when(tasks.findById(any())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            return Stream.of(t1, t2, t3, t4)
                    .filter(t -> t.getId().equals(id))
                    .findFirst();
        });
        when(tasks.findByBddStory(any())).thenAnswer(invocation -> {
            String bddStory = invocation.getArgument(0);
            return Stream.of(t1, t2, t3, t4)
                    .filter(task -> bddStory.equals(task.getBddStory()))
                    .toList();
        });
    }

    @Test
    void getBoardReturnsOrderedColumnsGroupedStoriesAndTasks() {
        BoardResponse board = service.getBoard(null, null);

        assertNotNull(board.activeProgramIncrementId());
        assertEquals("PI-1", board.programIncrements().getFirst().code());
        assertEquals(EXPECTED_COLUMNS, board.columns().stream().map(ColumnResponse::name).toList());
        assertEquals(3, board.sprints().size());
        long storyCount = board.columns().stream().mapToLong(c -> c.stories().size()).sum();
        long taskCount = board.columns().stream().mapToLong(c -> c.tasks().size()).sum();
        assertTrue(storyCount > 0, "Board must expose seeded stories");
        assertTrue(taskCount > 0, "Board must expose seeded tasks");
    }

    @Test
    void getBoardFiltersStoriesBySelectedSprint() {
        Long sprintId = 1L;

        BoardResponse board = service.getBoard(null, sprintId);

        assertEquals(sprintId, board.activeSprintId());
        long storyCount = board.columns().stream().mapToLong(c -> c.stories().size()).sum();
        assertNotEquals(0, storyCount, "Filtered sprint must expose stories");
        board.columns().stream()
                .flatMap(column -> column.stories().stream())
                .forEach(story -> assertEquals(sprintId, story.sprintId()));
    }

    void movingStoryToAnotherColumnPersists() {
        StoryCard story = service.getBoard(null, null).columns().stream()
                .flatMap(column -> column.stories().stream())
                .findFirst()
                .orElseThrow();
        String doneCode = "DONE";

        StoryCard moved = service.moveStory(story.id(),
                new MoveRequest(doneCode)).orElseThrow();

        assertEquals(story.id(), moved.id());
    }

    @Test
    void movingTaskToAnotherColumnPersists() {
        TaskCard task = service.getBoard(null, null).columns().stream()
                .flatMap(column -> column.tasks().stream())
                .findFirst()
                .orElseThrow();
        String reviewCode = "CODE_REVIEW";

        TaskCard moved = service.moveTask(task.id(),
                new MoveRequest(reviewCode)).orElseThrow();

        assertEquals(task.id(), moved.id());
    }

    @Test
    void recordingBddExecutionUpdatesStoryStatusAndTimestamp() {
        UserStory story = stories.findAllByOrderByCodeAsc().stream()
                .filter(s -> s.getBddStory() != null && !s.getBddStory().isBlank())
                .findFirst()
                .orElseThrow();

        BddExecutionResponse updated =
                service.recordExecution(story.getBddStory(), "PASSED");

        assertTrue(updated.storiesUpdated() >= 1);
        assertEquals("PASSED", story.getExecutionStatus().name());
        assertNotNull(story.getLastRunAt());
    }

    @Test
    void recordingUnknownBddStoryFails() {
        BddExecutionResponse updated =
                service.recordExecution("does_not_exist.story", "PASSED");

        assertEquals(0, updated.storiesUpdated());
        assertEquals(0, updated.tasksUpdated());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
