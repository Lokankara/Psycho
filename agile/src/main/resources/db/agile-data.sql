MERGE INTO kanban_column (code, name, position) KEY (code)
VALUES ('TO_DO', 'To Do', 0);
MERGE INTO kanban_column (code, name, position) KEY (code)
VALUES ('IN_PROGRESS', 'In Progress', 1);
MERGE INTO kanban_column (code, name, position) KEY (code)
VALUES ('CODE_REVIEW', 'Code Review', 2);
MERGE INTO kanban_column (code, name, position) KEY (code)
VALUES ('DONE', 'Done', 3);

MERGE INTO agile_program_increment (code, name, goal, start_date, end_date) KEY (code)
VALUES ('PI-1', 'PI-1 · Collective Unconscious Engine',
        'Ship deterministic scoring, BDD coverage and an agile delivery board',
        DATE '2026-10-01', DATE '2027-01-31');

MERGE INTO agile_sprint (code, program_increment_id, name, goal, start_date, end_date) KEY (code)
SELECT 'PI1-S1', id, 'PI1 Sprint 1', 'Deterministic scoring',
       DATE '2026-10-01', DATE '2026-10-14'
FROM agile_program_increment WHERE code = 'PI-1';
MERGE INTO agile_sprint (code, program_increment_id, name, goal, start_date, end_date) KEY (code)
SELECT 'PI1-S2', id, 'PI1 Sprint 2', 'BDD and execution history',
       DATE '2026-10-15', DATE '2026-10-28'
FROM agile_program_increment WHERE code = 'PI-1';
MERGE INTO agile_sprint (code, program_increment_id, name, goal, start_date, end_date) KEY (code)
SELECT 'PI1-S3', id, 'PI1 Sprint 3', 'Agile board delivery',
       DATE '2026-10-29', DATE '2026-11-11'
FROM agile_program_increment WHERE code = 'PI-1';

MERGE INTO agile_epic (code, program_increment_id, name, description) KEY (code)
SELECT 'EP-1', id, 'Scoring determinism',
       'Coordinate calculation must be reproducible for identical payloads'
FROM agile_program_increment WHERE code = 'PI-1';
MERGE INTO agile_epic (code, program_increment_id, name, description) KEY (code)
SELECT 'EP-2', id, 'BDD and E2E coverage',
       'JBehave stories, page objects and execution history export'
FROM agile_program_increment WHERE code = 'PI-1';
MERGE INTO agile_epic (code, program_increment_id, name, description) KEY (code)
SELECT 'EP-3', id, 'Kanban delivery board',
       'Interactive Trello-like board backed by the agile domain model'
FROM agile_program_increment WHERE code = 'PI-1';
MERGE INTO user_story (code, title, story_points, priority, bdd_story,
                       acceptance_criteria, execution_status,
                       epic_id, sprint_id, kanban_column_id) KEY (code)
SELECT 'US-1', 'Verify coordinate determinism', 3, 'HIGH',
       'verify_coordinate_calculation.story',
       CONCAT('Given an identical set of quiz answers', CHAR(10),
              'When the assessment is scored twice', CHAR(10),
              'Then both runs return identical X, Y, Z coordinates and the same octant'),
       'NOT_RUN',
       (SELECT id FROM agile_epic WHERE code = 'EP-1'),
       (SELECT id FROM agile_sprint WHERE code = 'PI1-S1'),
       (SELECT id FROM kanban_column WHERE code = 'DONE');
MERGE INTO user_story (code, title, story_points, priority, bdd_story,
                       acceptance_criteria, execution_status,
                       epic_id, sprint_id, kanban_column_id) KEY (code)
SELECT 'US-2', 'Export execution history as JSON', 5, 'HIGH', NULL,
       CONCAT('Given a completed assessment', CHAR(10),
              'When the execution trace is recorded', CHAR(10),
              'Then a result_YYYYMMDD_HHMMSS.json file exists under task/history'),
       'NOT_RUN',
       (SELECT id FROM agile_epic WHERE code = 'EP-1'),
       (SELECT id FROM agile_sprint WHERE code = 'PI1-S2'),
       (SELECT id FROM kanban_column WHERE code = 'IN_PROGRESS');
MERGE INTO user_story (code, title, story_points, priority, bdd_story,
                       acceptance_criteria, execution_status,
                       epic_id, sprint_id, kanban_column_id) KEY (code)
SELECT 'US-3', 'Page objects and locator registry', 3, 'MEDIUM', NULL,
       CONCAT('Given a page object for each UI screen', CHAR(10),
              'When BDD steps reference selectors', CHAR(10),
              'Then no raw CSS or XPath literal appears in a step definition'),
       'NOT_RUN',
       (SELECT id FROM agile_epic WHERE code = 'EP-2'),
       (SELECT id FROM agile_sprint WHERE code = 'PI1-S2'),
       (SELECT id FROM kanban_column WHERE code = 'CODE_REVIEW');
MERGE INTO user_story (code, title, story_points, priority, bdd_story,
                       acceptance_criteria, execution_status,
                       epic_id, sprint_id, kanban_column_id) KEY (code)
SELECT 'US-4', 'Interactive kanban board', 5, 'MEDIUM', NULL,
       CONCAT('Given a PI and sprint selector', CHAR(10),
              'When a story is dragged between columns', CHAR(10),
              'Then the new column is persisted and survives a reload'),
       'NOT_RUN',
       (SELECT id FROM agile_epic WHERE code = 'EP-3'),
       (SELECT id FROM agile_sprint WHERE code = 'PI1-S3'),
       (SELECT id FROM kanban_column WHERE code = 'TO_DO');

MERGE INTO agile_task (code, title, bdd_story, execution_status,
                       user_story_id, kanban_column_id) KEY (code)
SELECT 'T-1', 'Round coordinates to two decimals', 'verify_coordinate_calculation.story',
       'NOT_RUN',
       (SELECT id FROM user_story WHERE code = 'US-1'),
       (SELECT id FROM kanban_column WHERE code = 'DONE');
MERGE INTO agile_task (code, title, bdd_story, execution_status,
                       user_story_id, kanban_column_id) KEY (code)
SELECT 'T-2', 'Write HistoryResult for task/history', NULL, 'NOT_RUN',
       (SELECT id FROM user_story WHERE code = 'US-2'),
       (SELECT id FROM kanban_column WHERE code = 'IN_PROGRESS');
MERGE INTO agile_task (code, title, bdd_story, execution_status,
                       user_story_id, kanban_column_id) KEY (code)
SELECT 'T-3', 'Drag and drop column transitions', NULL, 'NOT_RUN',
       (SELECT id FROM user_story WHERE code = 'US-4'),
       (SELECT id FROM kanban_column WHERE code = 'CODE_REVIEW');
MERGE INTO agile_task (code, title, bdd_story, execution_status,
                       user_story_id, kanban_column_id) KEY (code)
SELECT 'T-4', 'Centralise selectors in Locators', NULL, 'NOT_RUN',
       (SELECT id FROM user_story WHERE code = 'US-3'),
       (SELECT id FROM kanban_column WHERE code = 'TO_DO');

