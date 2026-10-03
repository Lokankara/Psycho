package com.unconscious.collective.bdd;

import com.unconscious.collective.bdd.dto.AgileDtos.BoardResponse;
import com.unconscious.collective.bdd.dto.AgileDtos.BddExecutionResponse;
import com.unconscious.collective.bdd.dto.AgileDtos.ColumnResponse;
import com.unconscious.collective.bdd.dto.AgileDtos.MoveRequest;
import com.unconscious.collective.bdd.dto.AgileDtos.ProgramIncrementOption;
import com.unconscious.collective.bdd.dto.AgileDtos.SprintOption;
import com.unconscious.collective.bdd.dto.AgileDtos.StoryCard;
import com.unconscious.collective.bdd.dto.AgileDtos.TaskCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AgileBoardService {

    private static final Logger LOG = LoggerFactory.getLogger(AgileBoardService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private final ProgramIncrementRepository programIncrements;
    private final SprintRepository sprints;
    private final UserStoryRepository stories;
    private final TaskRepository tasks;
    private final KanbanColumnRepository columns;

    public AgileBoardService(ProgramIncrementRepository programIncrements, SprintRepository sprints,
                             UserStoryRepository stories, TaskRepository tasks,
                             KanbanColumnRepository columns) {
        this.programIncrements = programIncrements;
        this.sprints = sprints;
        this.stories = stories;
        this.tasks = tasks;
        this.columns = columns;
    }

    @Transactional(readOnly = true)
    public BoardResponse board(Long programIncrementId, Long sprintId) {
        List<ProgramIncrement> allPis = programIncrements.findAllByOrderByStartDateAsc();
        ProgramIncrement activePi = selectPi(allPis, programIncrementId);
        List<Sprint> piSprints = activePi == null
                ? List.of()
                : sprints.findByProgramIncrementIdOrderByStartDateAsc(activePi.getId());
        Sprint activeSprint = selectSprint(piSprints, sprintId);
        List<UserStory> visible = activeSprint == null
                ? stories.findAllByOrderByCodeAsc()
                : stories.findBySprintIdOrderByCodeAsc(activeSprint.getId());

        Map<String, List<StoryCard>> storyGroups = groupStories(visible);
        Map<String, List<TaskCard>> taskGroups = groupTasks(tasks.findAllByOrderByCodeAsc());
        List<ColumnResponse> columnResponses = columns.findAllByOrderByPositionAsc().stream()
                .map(column -> new ColumnResponse(
                        column.getCode(), column.getName(), column.getPosition(),
                        storyGroups.getOrDefault(column.getCode(), List.of()),
                        taskGroups.getOrDefault(column.getCode(), List.of())))
                .toList();

        return new BoardResponse(
                allPis.stream().map(this::piOption).toList(),
                piSprints.stream().map(this::sprintOption).toList(),
                activePi == null ? null : activePi.getId(),
                activeSprint == null ? null : activeSprint.getId(),
                columnResponses);
    }

    @Transactional
    public Optional<StoryCard> moveStory(Long storyId, MoveRequest request) {
        return stories.findById(storyId).map(story -> {
            story.setKanbanColumn(requireColumn(request.columnCode()));
            stories.save(story);
            LOG.info("Story {} moved to {}", story.getCode(), story.getKanbanColumn().getCode());
            return storyCard(story);
        });
    }

    @Transactional
    public Optional<TaskCard> moveTask(Long taskId, MoveRequest request) {
        return tasks.findById(taskId).map(task -> {
            task.setKanbanColumn(requireColumn(request.columnCode()));
            tasks.save(task);
            LOG.info("Task {} moved to {}", task.getCode(), task.getKanbanColumn().getCode());
            return taskCard(task);
        });
    }

    @Transactional
    public BddExecutionResponse recordExecution(String bddStory, String statusRaw) {
        BddStatus status = parseStatus(statusRaw);
        Instant runAt = Instant.now();
        int updatedStories = 0;
        for (UserStory story : stories.findByBddStory(bddStory).stream().toList()) {
            story.setExecutionStatus(status);
            story.setLastRunAt(runAt);
            stories.save(story);
            updatedStories++;
        }
        int updatedTasks = 0;
        for (Task task : tasks.findByBddStory(bddStory).stream().toList()) {
            task.setExecutionStatus(status);
            task.setLastRunAt(runAt);
            tasks.save(task);
            updatedTasks++;
        }
        LOG.info("BDD {} -> {} for {} stories and {} tasks", bddStory, status, updatedStories, updatedTasks);
        return new BddExecutionResponse(updatedStories, updatedTasks);
    }

    private KanbanColumn requireColumn(String columnCode) {
        if (columnCode == null || columnCode.isBlank()) {
            throw new IllegalArgumentException("columnCode must not be blank");
        }
        return columns.findByCode(columnCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new IllegalArgumentException("Unknown kanban column: " + columnCode));
    }

    private static BddStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("status must not be blank");
        }
        try {
            return BddStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown bdd status: " + raw, e);
        }
    }

    private static ProgramIncrement selectPi(List<ProgramIncrement> candidates, Long requestedId) {
        if (requestedId != null) {
            return candidates.stream()
                    .filter(pi -> requestedId.equals(pi.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private static Sprint selectSprint(List<Sprint> candidates, Long requestedId) {
        if (requestedId != null) {
            return candidates.stream()
                    .filter(sprint -> requestedId.equals(sprint.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private Map<String, List<StoryCard>> groupStories(List<UserStory> visible) {
        Map<String, List<StoryCard>> grouped = new LinkedHashMap<>();
        for (UserStory story : visible) {
            grouped.computeIfAbsent(story.getKanbanColumn().getCode(), key -> new ArrayList<>())
                    .add(storyCard(story));
        }
        return grouped;
    }

    private Map<String, List<TaskCard>> groupTasks(List<Task> allTasks) {
        Map<String, List<TaskCard>> grouped = new LinkedHashMap<>();
        for (Task task : allTasks) {
            grouped.computeIfAbsent(task.getKanbanColumn().getCode(), key -> new ArrayList<>())
                    .add(taskCard(task));
        }
        return grouped;
    }

    private ProgramIncrementOption piOption(ProgramIncrement pi) {
        return new ProgramIncrementOption(pi.getId(), pi.getCode(), pi.getName(), pi.getGoal());
    }

    private SprintOption sprintOption(Sprint sprint) {
        return new SprintOption(sprint.getId(), sprint.getCode(), sprint.getName(), sprint.getGoal(),
                sprint.getStartDate() == null ? null : sprint.getStartDate().toString(),
                sprint.getEndDate() == null ? null : sprint.getEndDate().toString());
    }

    private StoryCard storyCard(UserStory story) {
        return new StoryCard(story.getId(), story.getCode(), story.getTitle(), story.getStoryPoints(),
                story.getPriority(),
                story.getSprint() == null ? null : story.getSprint().getId(),
                story.getEpic() == null ? null : story.getEpic().getId(),
                story.getAcceptanceCriteria(), story.getBddStory(), story.getExecutionStatus().name(),
                story.getLastRunAt() == null ? null : ISO.format(story.getLastRunAt()));
    }

    private TaskCard taskCard(Task task) {
        return new TaskCard(task.getId(), task.getCode(), task.getTitle(),
                task.getUserStory() == null ? null : task.getUserStory().getId(),
                task.getBddStory(), task.getExecutionStatus().name(),
                task.getLastRunAt() == null ? null : ISO.format(task.getLastRunAt()));
    }
}
