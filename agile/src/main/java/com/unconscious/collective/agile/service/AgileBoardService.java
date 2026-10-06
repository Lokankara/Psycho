package com.unconscious.collective.agile.service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.unconscious.collective.agile.dao.KanbanColumnRepository;
import com.unconscious.collective.agile.dao.ProgramIncrementRepository;
import com.unconscious.collective.agile.dao.SprintRepository;
import com.unconscious.collective.agile.dao.TaskRepository;
import com.unconscious.collective.agile.dao.UserStoryRepository;
import com.unconscious.collective.agile.model.dto.BddExecutionResponse;
import com.unconscious.collective.agile.model.dto.BoardResponse;
import com.unconscious.collective.agile.model.dto.ColumnResponse;
import com.unconscious.collective.agile.model.dto.MoveRequest;
import com.unconscious.collective.agile.model.dto.ProgramIncrementOption;
import com.unconscious.collective.agile.model.dto.SprintOption;
import com.unconscious.collective.agile.model.dto.StoryCard;
import com.unconscious.collective.agile.model.dto.TaskCard;
import com.unconscious.collective.agile.model.entity.BddStatus;
import com.unconscious.collective.agile.model.entity.KanbanColumn;
import com.unconscious.collective.agile.model.entity.ProgramIncrement;
import com.unconscious.collective.agile.model.entity.Sprint;
import com.unconscious.collective.agile.model.entity.Task;
import com.unconscious.collective.agile.model.entity.UserStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class AgileBoardService implements IAgileBoardOperations {

    private static final Logger LOG = LoggerFactory.getLogger(AgileBoardService.class);
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    private final ProgramIncrementRepository incrementRepository;
    private final SprintRepository sprintRepository;
    private final UserStoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final KanbanColumnRepository columnRepository;

    public AgileBoardService(ProgramIncrementRepository incrementRepository, SprintRepository sprintRepository,
            UserStoryRepository storyRepository, TaskRepository taskRepository,
            KanbanColumnRepository columnRepository) {
        this.incrementRepository = incrementRepository;
        this.sprintRepository = sprintRepository;
        this.storyRepository = storyRepository;
        this.taskRepository = taskRepository;
        this.columnRepository = columnRepository;
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long programIncrementId, Long sprintId) {
        List<ProgramIncrement> allPis = incrementRepository.findAllByOrderByStartDateAsc();
        ProgramIncrement activePi = selectPi(allPis, programIncrementId);
        List<Sprint> piSprints = activePi == null
                ? List.of()
                : sprintRepository.findByProgramIncrementIdOrderByStartDateAsc(activePi.getId());
        Sprint activeSprint = selectSprint(piSprints, sprintId);
        List<UserStory> visible = activeSprint == null
                ? storyRepository.findAllByOrderByCodeAsc()
                : storyRepository.findBySprintIdOrderByCodeAsc(activeSprint.getId());

        Map<String, List<StoryCard>> storyGroups = groupStories(visible);
        List<Task> visibleTasks = activeSprint == null
                ? taskRepository.findAllByOrderByCodeAsc()
                : taskRepository.findByUserStorySprintIdOrderByCodeAsc(activeSprint.getId());
        Map<String, List<TaskCard>> taskGroups = groupTasks(visibleTasks);
        List<ColumnResponse> columnResponses = columnRepository.findAllByOrderByPositionAscOrDefault().stream()
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
        return storyRepository.findById(storyId).map(story -> {
            story.setKanbanColumn(requireColumn(request.columnCode()));
            UserStory saved = storyRepository.save(story);
            LOG.info("Story {} moved to {}", saved.getCode(), saved.getKanbanColumn().getCode());
            return storyCard(story);
        });
    }

    @Transactional
    public Optional<TaskCard> moveTask(Long taskId, MoveRequest request) {
        return taskRepository.findById(taskId).map(task -> {
            task.setKanbanColumn(requireColumn(request.columnCode()));
            taskRepository.save(task);
            LOG.info("Task {} moved to {}", task.getCode(), task.getKanbanColumn().getCode());
            return taskCard(task);
        });
    }

    @Transactional
    public BddExecutionResponse recordExecution(String bddStory, String statusRaw) {
        BddStatus status = parseStatus(statusRaw);
        Instant runAt = Instant.now();
        int updatedStories = 0;
        for (UserStory story : storyRepository.findByBddStory(bddStory)) {
            story.setExecutionStatus(status);
            story.setLastRunAt(runAt);
            storyRepository.save(story);
            updatedStories++;
        }
        int updatedTasks = 0;
        for (Task task : taskRepository.findByBddStory(bddStory)) {
            task.setExecutionStatus(status);
            task.setLastRunAt(runAt);
            taskRepository.save(task);
            updatedTasks++;
        }
        LOG.info("BDD {} -> {} for {} stories and {} tasks", bddStory, status, updatedStories, updatedTasks);
        return new BddExecutionResponse(updatedStories, updatedTasks);
    }

    private KanbanColumn requireColumn(String columnCode) {
        if (columnCode == null || columnCode.isBlank()) {
            throw new IllegalArgumentException("columnCode must not be blank");
        }
        return columnRepository.findByCode(columnCode.trim().toUpperCase(Locale.ROOT))
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
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private static Sprint selectSprint(List<Sprint> candidates, Long requestedId) {
        if (requestedId != null) {
            return candidates.stream()
                    .filter(sprint -> requestedId.equals(sprint.getId()))
                    .findFirst()
                    .orElse(null);
        }
        return candidates.isEmpty() ? null : candidates.getFirst();
    }

    private Map<String, List<StoryCard>> groupStories(List<UserStory> visible) {
        return visible.stream().collect(
                Collectors.groupingBy(story -> story.getKanbanColumn().getCode(), LinkedHashMap::new,
                        Collectors.mapping(this::storyCard, Collectors.toList())));
    }

    private Map<String, List<TaskCard>> groupTasks(List<Task> allTasks) {
        return allTasks.stream().collect(
                Collectors.groupingBy(task -> task.getKanbanColumn().getCode(), LinkedHashMap::new,
                        Collectors.mapping(this::taskCard, Collectors.toList())));
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
