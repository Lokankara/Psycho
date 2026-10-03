# PROGRESS — Iteration 2: CI/CD, History Logging, BDD E2E, Kanban

## Phases & Planned Modifications

### Phase 0 — Bug Fix (already complete, verifying)
- `ArchetypeScoringService.score()` already isolated all accumulators as locals, 2-decimal rounding, null-pole handling, dedup by question ID.
- `AssessmentPayload.answersFor()` already skips null poles and deduplicates.
- `ResultService` delegates scoring to `ArchetypeScoringService` (no manual HashMap loop).
- **Action**: Add `ArchetypeScoringServiceDeterminismTest` to lock the fix with a regression test.

### Phase 1 — Wire HistoryLogger into ResultService
- Inject `HistoryLogger` into `ResultService` constructor.
- Call `historyLogger.write(ExecutionTrace.of(payload, profile))` inside `analyzeAndSave()` after scoring, before/after persistence.
- **Files**: `rest/.../service/ResultService.java`

### Phase 2 — Bug Investigation & Reporting
- Diff the historical `-1.00 vs -0.67` Z-axis records, validate 3 hypotheses (floating-point, missing axis answers, state leakage).
- **File**: `task/bug_report_coordinates.md`

### Phase 3 — JBehave BDD + E2E
- Add `org.jbehave:jbehive-core:5.1.0` (jupiter engine) to `rest/build.gradle`.
- Create Gradle task `e2eTest` (JUnit Platform, separate from `test`).
- Story: `rest/src/test/resources/stories/verify_coordinate_calculation.story`
- Step definitions under `rest/src/test/java/.../bdd/steps/`
- `Locators` holder class under `rest/src/test/java/.../bdd/pages/` (QuizViewPage, DashboardPage, AxisControlsPage POM classes referencing Locators — no inline selectors in steps)
- **Files**: `rest/build.gradle`, story, step defs, POM, locators

### Phase 4 — Artifacts
- `task/user_story_acceptance.md` — Given/When/Then AC
- `task/bug_report_coordinates.md` — RCA + JSON diffs
- `task/bdd_specifications.md` — step↔page↔locator catalog

### Phase 5 — bdd module build + wiring
- Create `bdd/build.gradle` (Spring Boot library: spring-boot-starter-data-jpa, lombok, h2/runtime).
- Add `implementation project(':bdd')` to `rest/build.gradle`.
- Add `@EntityScan(basePackages = {"com.unconscious.collective.quiz", "com.unconscious.collective.bdd"})` to `QuizApplication`.
- Ensure H2 schema includes agile tables (ddl-auto=update handles this).
- **Files**: `bdd/build.gradle`, `rest/build.gradle`, `QuizApplication.java`

### Phase 6 — Kanban UI (already complete)
- React `/board` page, `@dnd-kit` drag-and-drop, PI/Sprint selectors, detail drawer — all present in `ui/src/kanban/`.
- **Action**: Verify wiring is correct, no changes needed.

### Phase 7 — Validation
- `./gradlew test` (unit + JPA)
- `./gradlew e2eTest` (JBehave stories + history JSON)
- `npm run build`
