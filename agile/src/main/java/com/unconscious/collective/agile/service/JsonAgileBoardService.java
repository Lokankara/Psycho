package com.unconscious.collective.agile.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unconscious.collective.agile.model.dto.BddExecutionResponse;
import com.unconscious.collective.agile.model.dto.BoardResponse;
import com.unconscious.collective.agile.model.dto.ColumnResponse;
import com.unconscious.collective.agile.model.dto.MoveRequest;
import com.unconscious.collective.agile.model.dto.ProgramIncrementOption;
import com.unconscious.collective.agile.model.dto.SprintOption;
import com.unconscious.collective.agile.model.dto.StoryCard;
import com.unconscious.collective.agile.model.dto.TaskCard;
import com.unconscious.collective.agile.model.entity.BddStatus;
import com.unconscious.collective.agile.model.entity.Epic;
import com.unconscious.collective.agile.model.entity.KanbanColumn;
import com.unconscious.collective.agile.model.entity.ProgramIncrement;
import com.unconscious.collective.agile.model.entity.Sprint;
import com.unconscious.collective.agile.model.entity.Task;
import com.unconscious.collective.agile.model.entity.UserStory;
import com.unconscious.collective.agile.model.json.JsonEpic;
import com.unconscious.collective.agile.model.json.JsonInitData;
import com.unconscious.collective.agile.model.json.JsonKanbanColumn;
import com.unconscious.collective.agile.model.json.JsonProgramIncrement;
import com.unconscious.collective.agile.model.json.JsonSprint;
import com.unconscious.collective.agile.model.json.JsonTask;
import com.unconscious.collective.agile.model.json.JsonTrelloBoard;
import com.unconscious.collective.agile.model.json.JsonTrelloCard;
import com.unconscious.collective.agile.model.json.JsonTrelloLabel;
import com.unconscious.collective.agile.model.json.JsonTrelloList;
import com.unconscious.collective.agile.model.json.JsonUserStory;
import org.springframework.core.io.ResourceLoader;
import tools.jackson.databind.ObjectMapper;

public class JsonAgileBoardService implements IAgileBoardOperations {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;
    private static final Pattern TIMESTAMP =
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4},\\s*\\d{2}:\\d{2}:\\d{2})");
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private final List<KanbanColumn> columns = new ArrayList<>();
    private final List<ProgramIncrement> programIncrements = new ArrayList<>();
    private final List<Sprint> sprints = new ArrayList<>();
    private final List<Epic> epics = new ArrayList<>();
    private final List<UserStory> userStories = new ArrayList<>();
    private final List<Task> tasks = new ArrayList<>();

    public JsonAgileBoardService(ResourceLoader resourceLoader) {
        loadData(resourceLoader);
    }

    private void loadData(ResourceLoader resourceLoader) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = resourceLoader.getResource("classpath:json/bugs/init.json").getInputStream()) {
            byte[] bytes = in.readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);
            JsonInitData data = mapper.readValue(json, JsonInitData.class);
            loadColumns(data.kanbanColumns());
            loadProgramIncrements(data.programIncrements());
            loadEpics(data.programIncrements());
            loadUserStories(data.userStories());
            loadTasks(data.tasks());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load json/bugs/init.json", e);
        }
        loadTrelloBoard(resourceLoader, mapper, "classpath:json/story/allure.json", true);
        loadTrelloBoard(resourceLoader, mapper, "classpath:json/task/allure.json", false);
        loadTrelloBoard(resourceLoader, mapper, "classpath:json/bugs/coordinates.json", false);
    }

    private void loadTrelloBoard(ResourceLoader resourceLoader, ObjectMapper mapper, String location,
            boolean asStory) {
        try (InputStream in = resourceLoader.getResource(location).getInputStream()) {
            byte[] bytes = in.readAllBytes();
            String json = new String(bytes, StandardCharsets.UTF_8);
            JsonTrelloBoard board = mapper.readValue(json, JsonTrelloBoard.class);
            importTrelloBoard(board, location, asStory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + location, e);
        }
    }

    private void importTrelloBoard(JsonTrelloBoard board, String source, boolean asStory) {
        if (board == null || board.cards() == null || board.cards().isEmpty()) {
            return;
        }
        Sprint defaultSprint = sprints.isEmpty() ? null : sprints.getFirst();
        Epic defaultEpic = epics.isEmpty() ? null : epics.getFirst();
        Map<String, String> listToColumn = new LinkedHashMap<>();
        if (board.lists() != null) {
            for (JsonTrelloList list : board.lists()) {
                if (list == null || list.id() == null) {
                    continue;
                }
                listToColumn.put(list.id(), mapListNameToColumnCode(list.name()));
            }
        }
        for (JsonTrelloCard card : board.cards()) {
            if (card == null || Boolean.TRUE.equals(card.closed())) {
                continue;
            }
            KanbanColumn column = columnByCode(listToColumn.getOrDefault(card.idList(), "TO_DO"));
            if (column == null) {
                continue;
            }
            if (asStory && !isRunCard(card)) {
                importTrelloCardAsStory(board, card, source, column, defaultSprint, defaultEpic);
            } else {
                importTrelloCardAsTask(board, card, source, column);
            }
        }
    }

    private static boolean isRunCard(JsonTrelloCard card) {
        String id = card.id() == null ? "" : card.id().toLowerCase(Locale.ROOT);
        String name = card.name() == null ? "" : card.name().toLowerCase(Locale.ROOT);
        return id.startsWith("run-") || name.startsWith("run #");
    }

    private static String mapListNameToColumnCode(String listName) {
        String name = listName == null ? "" : listName.toLowerCase(Locale.ROOT);
        if (name.contains("done") || name.contains("artifact") || name.contains("report")) {
            return "DONE";
        }
        if (name.contains("review") || name.contains("ready")) {
            return "CODE_REVIEW";
        }
        if (name.contains("progress") || name.contains("run") || name.contains("drift")
                || name.contains("investigation")) {
            return "IN_PROGRESS";
        }
        return "TO_DO";
    }

    private void importTrelloCardAsStory(JsonTrelloBoard board, JsonTrelloCard card, String source,
            KanbanColumn column, Sprint defaultSprint, Epic defaultEpic) {
        UserStory story = new UserStory();
        story.setId(nextStoryId());
        story.setCode(storyCode(card));
        story.setTitle(card.name() == null ? card.id() : card.name());
        story.setDescription(trelloDescription(board, card, source));
        story.setAcceptanceCriteria(card.desc());
        story.setStoryPoints(null);
        story.setPriority(trelloPriority(card));
        story.setExecutionStatus(trelloStatus(card));
        story.setLastRunAt(trelloRunAt(card));
        story.setEpic(defaultEpic);
        story.setSprint(defaultSprint);
        story.setKanbanColumn(column);
        userStories.add(story);
    }

    private void importTrelloCardAsTask(JsonTrelloBoard board, JsonTrelloCard card, String source,
            KanbanColumn column) {
        Task task = new Task();
        task.setId(nextTaskId());
        task.setCode(storyCode(card));
        task.setTitle(card.name() == null ? card.id() : card.name());
        task.setDescription(trelloDescription(board, card, source));
        task.setExecutionStatus(trelloStatus(card));
        task.setLastRunAt(trelloRunAt(card));
        task.setUserStory(null);
        task.setKanbanColumn(column);
        tasks.add(task);
    }

    private void loadColumns(List<JsonKanbanColumn> jsonColumns) {
        for (JsonKanbanColumn jc : jsonColumns) {
            KanbanColumn col = new KanbanColumn();
            col.setId(generateId());
            col.setCode(jc.code());
            col.setName(jc.name());
            col.setPosition(jc.position());
            columns.add(col);
        }
    }

    private void loadProgramIncrements(List<JsonProgramIncrement> jsonPis) {
        for (JsonProgramIncrement jpi : jsonPis) {
            ProgramIncrement pi = new ProgramIncrement();
            pi.setId(generateId());
            pi.setCode(jpi.code());
            pi.setName(jpi.name());
            pi.setGoal(jpi.goal());
            pi.setStartDate(LocalDate.parse(jpi.startDate()));
            pi.setEndDate(LocalDate.parse(jpi.endDate()));
            programIncrements.add(pi);

            if (jpi.sprints() != null) {
                for (JsonSprint js : jpi.sprints()) {
                    Sprint sprint = new Sprint();
                    sprint.setId(generateId());
                    sprint.setCode(js.code());
                    sprint.setName(js.name());
                    sprint.setGoal(js.goal());
                    sprint.setStartDate(LocalDate.parse(js.startDate()));
                    sprint.setEndDate(LocalDate.parse(js.endDate()));
                    sprint.setProgramIncrement(pi);
                    sprints.add(sprint);
                }
            }
        }
    }

    private void loadEpics(List<JsonProgramIncrement> jsonPis) {
        for (JsonProgramIncrement jpi : jsonPis) {
            if (jpi.epics() != null) {
                for (JsonEpic je : jpi.epics()) {
                    Epic epic = new Epic();
                    epic.setId(generateId());
                    epic.setCode(je.code());
                    epic.setName(je.name());
                    epic.setGoal(je.description());
                    epic.setDescription(je.description());
                    programIncrements.stream()
                            .filter(p -> p.getCode().equals(jpi.code()))
                            .findFirst().ifPresent(epic::setProgramIncrement);
                    epics.add(epic);
                }
            }
        }
    }

    private void loadUserStories(List<JsonUserStory> jsonStories) {
        for (JsonUserStory jus : jsonStories) {
            UserStory story = new UserStory();
            story.setId(generateId());
            story.setCode(jus.code());
            story.setTitle(jus.title());
            story.setStoryPoints(jus.storyPoints());
            story.setPriority(jus.priority());
            story.setBddStory(jus.bddStory());
            story.setAcceptanceCriteria(jus.acceptanceCriteria());
            story.setExecutionStatus(BddStatus.valueOf(jus.executionStatus()));
            story.setLastRunAt(null);

            epics.stream().filter(e -> e.getCode().equals(jus.epicCode()))
                    .findFirst().ifPresent(story::setEpic);

            sprints.stream()
                    .filter(s -> s.getCode().equals(jus.sprintCode()))
                    .findFirst().ifPresent(story::setSprint);

            columns.stream()
                    .filter(c -> c.getCode().equals(jus.kanbanColumnCode()))
                    .findFirst().ifPresent(story::setKanbanColumn);

            userStories.add(story);
        }
    }

    private void loadTasks(List<JsonTask> jsonTasks) {
        for (JsonTask jt : jsonTasks) {
            Task task = new Task();
            task.setId(generateId());
            task.setCode(jt.code());
            task.setTitle(jt.title());
            task.setBddStory(jt.bddStory());
            task.setExecutionStatus(BddStatus.valueOf(jt.executionStatus()));
            task.setLastRunAt(null);

            userStories.stream()
                    .filter(s -> s.getCode().equals(jt.userStoryCode()))
                    .findFirst().ifPresent(task::setUserStory);

            columns.stream()
                    .filter(c -> c.getCode().equals(jt.kanbanColumnCode()))
                    .findFirst().ifPresent(task::setKanbanColumn);

            tasks.add(task);
        }
    }

    private Long generateId() {
        return (long) (new Random().nextInt(900000) + 100000);
    }

    public BoardResponse getBoard(Long programIncrementId, Long sprintId) {
        List<ProgramIncrement> allPis = programIncrements.stream()
                .sorted(Comparator.comparing(ProgramIncrement::getStartDate))
                .toList();

        ProgramIncrement activePi = selectPi(allPis, programIncrementId);
        List<Sprint> piSprints = activePi == null
                ? List.of()
                : sprints.stream()
                .filter(s -> s.getProgramIncrement() != null
                        && s.getProgramIncrement().getId().equals(activePi.getId()))
                .sorted(Comparator.comparing(Sprint::getStartDate))
                .toList();

        Sprint activeSprint = selectSprint(piSprints, sprintId);

        List<UserStory> visible = activeSprint == null
                ? userStories.stream().sorted(Comparator.comparing(UserStory::getCode)).toList()
                : userStories.stream()
                .filter(s -> s.getSprint() != null && s.getSprint().getId().equals(activeSprint.getId()))
                .sorted(Comparator.comparing(UserStory::getCode))
                .toList();

        Map<String, List<StoryCard>> storyGroups = groupStories(visible);
        List<Task> visibleTasks = activeSprint == null
                ? tasks.stream().sorted(Comparator.comparing(Task::getCode)).toList()
                : tasks.stream()
                .filter(t -> t.getUserStory() != null
                        && t.getUserStory().getSprint() != null
                        && t.getUserStory().getSprint().getId().equals(activeSprint.getId()))
                .sorted(Comparator.comparing(Task::getCode))
                .toList();
        Map<String, List<TaskCard>> taskGroups = groupTasks(visibleTasks);

        List<ColumnResponse> columnResponses = columns.stream()
                .sorted(Comparator.comparingInt(KanbanColumn::getPosition))
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

    public Optional<StoryCard> moveStory(Long storyId, MoveRequest request) {
        return userStories.stream()
                .filter(s -> s.getId().equals(storyId))
                .findFirst()
                .map(story -> {
                    KanbanColumn column = columns.stream()
                            .filter(c -> c.getCode().equalsIgnoreCase(request.columnCode()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown column: " + request.columnCode()));
                    story.setKanbanColumn(column);
                    return storyCard(story);
                });
    }

    public Optional<TaskCard> moveTask(Long taskId, MoveRequest request) {
        return tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .map(task -> {
                    KanbanColumn column = columns.stream()
                            .filter(c -> c.getCode().equalsIgnoreCase(request.columnCode()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Unknown column: " + request.columnCode()));
                    task.setKanbanColumn(column);
                    return taskCard(task);
                });
    }

    public BddExecutionResponse recordExecution(String bddStory, String statusRaw) {
        BddStatus status = BddStatus.valueOf(statusRaw.toUpperCase());
        Instant runAt = Instant.now();
        int updatedStories = 0;
        for (UserStory story : userStories) {
            if (bddStory.equals(story.getBddStory())) {
                story.setExecutionStatus(status);
                story.setLastRunAt(runAt);
                updatedStories++;
            }
        }
        int updatedTasks = 0;
        for (Task task : tasks) {
            if (bddStory.equals(task.getBddStory())) {
                task.setExecutionStatus(status);
                task.setLastRunAt(runAt);
                updatedTasks++;
            }
        }
        return new BddExecutionResponse(updatedStories, updatedTasks);
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
        Map<String, List<StoryCard>> grouped = new LinkedHashMap<>();
        for (UserStory story : visible) {
            String code = story.getKanbanColumn() != null ? story.getKanbanColumn().getCode() : null;
            if (code != null) {
                grouped.computeIfAbsent(code, key -> new ArrayList<>()).add(storyCard(story));
            }
        }
        return grouped;
    }

    private Map<String, List<TaskCard>> groupTasks(List<Task> allTasks) {
        Map<String, List<TaskCard>> grouped = new LinkedHashMap<>();
        for (Task task : allTasks) {
            String code = task.getKanbanColumn() != null ? task.getKanbanColumn().getCode() : null;
            if (code != null) {
                grouped.computeIfAbsent(code, key -> new ArrayList<>()).add(taskCard(task));
            }
        }
        return grouped;
    }

    private static String uniqueCode(String prefix, String raw, Set<String> reserved) {
        String base = prefix + sanitizeCode(raw);
        if (!reserved.contains(base) && base.length() <= 32) {
            return base;
        }
        for (int i = 1; i < 1000; i++) {
            String candidate = base + "-" + i;
            if (candidate.length() > 32) {
                candidate = base.substring(0, 32 - ("-" + i).length()) + "-" + i;
            }
            if (!reserved.contains(candidate)) {
                return candidate;
            }
        }
        return base.substring(0, Math.min(base.length(), 32));
    }

    private static String sanitizeCode(String raw) {
        String clean = raw == null ? "CARD" : raw.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-");
        clean = clean.replaceAll("^-+|-+$", "");
        if (clean.isEmpty()) {
            clean = "CARD";
        }
        return clean.length() > 28 ? clean.substring(0, 28) : clean;
    }

    private static String trelloPriority(JsonTrelloCard card) {
        if (card.labels() != null) {
            for (JsonTrelloLabel label : card.labels()) {
                String name = label.name() == null ? "" : label.name().toLowerCase(Locale.ROOT);
                String color = label.color() == null ? "" : label.color().toLowerCase(Locale.ROOT);
                if (name.contains("high") || color.equals("red")) {
                    return "HIGH";
                }
            }
            for (JsonTrelloLabel label : card.labels()) {
                String name = label.name() == null ? "" : label.name().toLowerCase(Locale.ROOT);
                String color = label.color() == null ? "" : label.color().toLowerCase(Locale.ROOT);
                if (name.contains("medium") || name.contains("ready") || color.equals("yellow")
                        || color.equals("purple")) {
                    return "MEDIUM";
                }
            }
        }
        return "MEDIUM";
    }

    private static BddStatus trelloStatus(JsonTrelloCard card) {
        String haystack = ((card.desc() == null ? "" : card.desc()) + " "
                + labelText(card)).toLowerCase(Locale.ROOT);
        if (haystack.contains("failed") || haystack.contains("failure")) {
            return BddStatus.FAILED;
        }
        if (haystack.contains("passed") || haystack.contains("pass")) {
            return BddStatus.PASSED;
        }
        return BddStatus.NOT_RUN;
    }

    private static String labelText(JsonTrelloCard card) {
        if (card.labels() == null) {
            return "";
        }
        StringBuilder names = new StringBuilder();
        for (JsonTrelloLabel label : card.labels()) {
            if (label.name() != null) {
                names.append(label.name()).append(' ');
            }
        }
        return names.toString();
    }

    private static Instant trelloRunAt(JsonTrelloCard card) {
        if (card.desc() == null) {
            return null;
        }
        Matcher matcher = TIMESTAMP.matcher(card.desc());
        if (!matcher.find()) {
            return null;
        }
        return parseDateTime(matcher.group(1));
    }

    private static Instant parseDateTime(String text) {
        try {
            LocalDateTime local = LocalDateTime.parse(text.trim(), TIMESTAMP_FORMAT);
            return local.atZone(ZoneId.systemDefault()).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private KanbanColumn columnByCode(String code) {
        if (code == null) {
            return null;
        }
        return columns.stream()
                .filter(column -> code.equals(column.getCode()))
                .findFirst()
                .orElse(null);
    }

    private KanbanColumn columnFor(String code) {
        if (code == null) {
            return columns.getFirst();
        }
        return columns.stream()
                .filter(column -> code.equals(column.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown column: " + code));
    }

    private static String storyCodeFromTrelloId(String trelloId) {
        return "US-" + sanitizeTrelloId(trelloId);
    }

    private static String taskCodeFromTrelloId(String trelloId) {
        return "T-" + sanitizeTrelloId(trelloId);
    }

    private static String sanitizeTrelloId(String trelloId) {
        if (trelloId == null || trelloId.isBlank()) {
            return "UNKNOWN";
        }
        String sanitized = trelloId.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        if (sanitized.isEmpty()) {
            return "UNKNOWN";
        }
        return sanitized.length() > 28 ? sanitized.substring(0, 28).replaceAll("-+$", "") : sanitized;
    }

    private static String storyCode(JsonTrelloCard card) {
        return storyCodeFromTrelloId(card.id());
    }

    private static String trelloDescription(JsonTrelloBoard board, JsonTrelloCard card, String source) {
        String boardName = board == null || board.name() == null ? "" : board.name().trim();
        String desc = card.desc() == null ? "" : card.desc().trim();
        String prefix = source == null ? "" : source.trim();
        StringBuilder result = new StringBuilder();
        if (!prefix.isEmpty()) {
            result.append('[').append(prefix).append(']');
        }
        if (!boardName.isEmpty()) {
            if (!result.isEmpty()) {
                result.append(' ');
            }
            result.append(boardName);
        }
        if (!desc.isEmpty()) {
            if (!result.isEmpty()) {
                result.append('\n');
            }
            result.append(desc);
        }
        return result.toString();
    }

    private KanbanColumn trelloColumn(String listName) {
        String name = listName == null ? "" : listName.toLowerCase(Locale.ROOT);
        if (name.contains("progress")) {
            return columnFor("IN_PROGRESS");
        }
        if (name.contains("review") || name.contains("ready")) {
            return columnFor("CODE_REVIEW");
        }
        if (name.contains("done")) {
            return columnFor("DONE");
        }
        return columnFor(null);
    }

    private Sprint defaultSprint() {
        return sprints.isEmpty() ? null : sprints.getFirst();
    }

    private Epic defaultEpic() {
        return epics.isEmpty() ? null : epics.getFirst();
    }

    private Long nextStoryId() {
        return userStories.stream()
                .map(UserStory::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .map(max -> max + 1)
                .orElse((long) userStories.size() + 1);
    }

    private Long nextTaskId() {
        return tasks.stream()
                .map(Task::getId)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .map(max -> max + 1)
                .orElse((long) tasks.size() + 1);
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
