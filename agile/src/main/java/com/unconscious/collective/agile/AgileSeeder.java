package com.unconscious.collective.bdd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class AgileSeeder implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AgileSeeder.class);

    private final KanbanColumnRepository columns;
    private final ProgramIncrementRepository programIncrements;
    private final SprintRepository sprints;
    private final EpicRepository epics;
    private final UserStoryRepository stories;
    private final TaskRepository tasks;

    public AgileSeeder(KanbanColumnRepository columns, ProgramIncrementRepository programIncrements,
                       SprintRepository sprints, EpicRepository epics,
                       UserStoryRepository stories, TaskRepository tasks) {
        this.columns = columns;
        this.programIncrements = programIncrements;
        this.sprints = sprints;
        this.epics = epics;
        this.stories = stories;
        this.tasks = tasks;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (columns.count() > 0) {
            LOG.info("Agile board already seeded, skipping");
            return;
        }

        KanbanColumn toDo = columns.save(new KanbanColumn("TO_DO", "To Do", 0));
        KanbanColumn inProgress = columns.save(new KanbanColumn("IN_PROGRESS", "In Progress", 1));
        KanbanColumn codeReview = columns.save(new KanbanColumn("CODE_REVIEW", "Code Review", 2));
        KanbanColumn done = columns.save(new KanbanColumn("DONE", "Done", 3));

        ProgramIncrement pi = programIncrements.save(new ProgramIncrement(
                "PI-1",
                "PI-1 · Collective Unconscious Engine",
                "Ship deterministic scoring, BDD coverage and an agile delivery board",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2027, 1, 31)));

        Sprint sprint1 = sprints.save(new Sprint("PI1-S1", "PI1 Sprint 1", "Deterministic scoring",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 14), pi));
        Sprint sprint2 = sprints.save(new Sprint("PI1-S2", "PI1 Sprint 2", "BDD and execution history",
                LocalDate.of(2026, 10, 15), LocalDate.of(2026, 10, 28), pi));
        Sprint sprint3 = sprints.save(new Sprint("PI1-S3", "PI1 Sprint 3", "Agile board delivery",
                LocalDate.of(2026, 10, 29), LocalDate.of(2026, 11, 11), pi));

        Epic scoring = epics.save(new Epic("EP-1", "Scoring determinism",
                "Coordinate calculation must be reproducible for identical payloads", pi));
        Epic bdd = epics.save(new Epic("EP-2", "BDD and E2E coverage",
                "JBehave stories, page objects and execution history export", pi));
        Epic board = epics.save(new Epic("EP-3", "Kanban delivery board",
                "Interactive Trello-like board backed by the agile domain model", pi));

        UserStory us1 = saveStory("US-1", "Verify coordinate determinism", scoring, sprint1, done, 3, "HIGH",
                "verify_coordinate_calculation.story",
                """
                Given an identical set of quiz answers
                When the assessment is scored twice
                Then both runs return identical X, Y, Z coordinates and the same octant""");

        UserStory us2 = saveStory("US-2", "Export execution history as JSON", scoring, sprint2, inProgress, 5,
                "HIGH", null,
                """
                Given a completed assessment
                When the execution trace is recorded
                Then a result_YYYYMMDD_HHMMSS.json file exists under task/history""");

        UserStory us3 = saveStory("US-3", "Page objects and locator registry", bdd, sprint2, codeReview, 3,
                "MEDIUM", null,
                """
                Given a page object for each UI screen
                When BDD steps reference selectors
                Then no raw CSS or XPath literal appears in a step definition""");

        UserStory us4 = saveStory("US-4", "Interactive kanban board", board, sprint3, toDo, 5, "MEDIUM", null,
                """
                Given a PI and sprint selector
                When a story is dragged between columns
                Then the new column is persisted and survives a reload""");

        saveTask("T-1", "Round coordinates to two decimals", us1, done, "verify_coordinate_calculation.story");
        saveTask("T-2", "Write HistoryLogger for task/history", us2, inProgress, null);
        saveTask("T-3", "Drag and drop column transitions", us4, codeReview, null);
        saveTask("T-4", "Centralise selectors in Locators", us3, toDo, null);

        LOG.info("Seeded PI-1 with 3 sprints, 4 kanban columns, 4 stories and 4 tasks");
    }

    private UserStory saveStory(String code, String title, Epic epic, Sprint sprint, KanbanColumn column,
                                int points, String priority, String bddStory, String acceptanceCriteria) {
        UserStory story = new UserStory();
        story.setCode(code);
        story.setTitle(title);
        story.setAcceptanceCriteria(acceptanceCriteria);
        story.setStoryPoints(points);
        story.setPriority(priority);
        story.setBddStory(bddStory);
        story.setEpic(epic);
        story.setSprint(sprint);
        story.setKanbanColumn(column);
        return stories.save(story);
    }

    private void saveTask(String code, String title, UserStory owner, KanbanColumn column, String bddStory) {
        Task task = new Task();
        task.setCode(code);
        task.setTitle(title);
        task.setUserStory(owner);
        task.setKanbanColumn(column);
        task.setBddStory(bddStory);
        tasks.save(task);
    }
}
