# Iteration Artifacts (artifact_pi.md)

## Commit Messages

### Commit 1: fix(history): wire HistoryResult into ResultService
```
fix(history): wire HistoryResult into ResultService execution flow

- Correct HistoryResult/ExecutionTrace package from quiz.history to
  quiz.domain.history to match directory structure
- Inject HistoryResult into ResultService constructor
- Write ExecutionTrace JSON on every analyze() and analyzeAndSave() call
- Update ResultServiceTest to use @TempDir and verify trace file output
- Add analyzeWritesExecutionTraceJson test asserting JSON persistence

Resolves: coordinate calculation determinism via trace logging
```

### Commit 2: feat(bdd): add Gradle module for JBehive BDD tests and agile entities
```
feat(bdd): add build.gradle with JBehive + rest test dependency

- Create bdd/build.gradle with Spring Data JPA + Lombok + H2/Postgres
- Add JBehive core 5.1.0 as test dependency
- Add testImplementation project(':rest') for scoring service access
- Register e2eTest Gradle task (JUnit Platform, *E2ETest pattern)
- Disable bootJar/bootBuildImage (library module)
```

### Commit 3: feat(bdd): JBehive story, step definitions, POM and Locators
```
feat(bdd): add JBehive BDD test infrastructure

- Story: verify_coordinate_calculation.story (4 scenarios)
- Steps: CoordinateCalculationSteps (Given/When/Then annotations)
- Locators: centralized CSS/XPath selector registry (no inline selectors)
- POM: QuizViewPage, DashboardPage, AxisControlsPage
- Runner: VerifyCoordinateCalculationE2ETest (JUnit 5 @Test + Embedder)
```

### Commit 4: feat(rest): wire bdd agile entities into Spring Boot application
```
feat(rest): enable entity scanning for com.unconscious.collective.bdd

- Add implementation project(':bdd') to rest/build.gradle
- Add @EntityScan to QuizApplication for quiz + agile packages
- H2 ddl-auto=update creates agile tables at startup
- AgileSeeder seeds PI-1 with 3 sprints, 4 columns, 4 stories, 4 tasks
```

### Commit 5: docs(task): add iteration-2 artifacts
```
docs(task): add bug report, acceptance criteria, BDD specs, history JSON

- task/bug_report_coordinates.md: RCA of Z-axis drift (-1.00 vs -0.67)
- task/user_story_acceptance.md: 8 Given/When/Then acceptance criteria
- task/bdd_specifications.md: step↔page↔locator catalog
- task/history/: 3 sample execution trace JSON files
- PROGRESS.md, artifact_pi.md updated
```

---

## Test Results Summary

### Unit Tests (Gradle `test`)

| Module | Test Class | Tests | Status |
|--------|-----------|-------|--------|
| rest | ArchetypeScoringServiceTest | 4 | PASS |
| rest | ResultServiceTest | 4 | PASS (3 existing + 1 new) |
| rest | SemanticMatchingServiceTest | 2 | PASS |
| rest | QuizApplicationTests | 1 | PASS (context load) |
| bdd | (none — library module) | 0 | — |

### E2E Tests (Gradle `e2eTest`)

| Story | Scenarios | Status |
|-------|-----------|--------|
| verify_coordinate_calculation.story | 4 | PASS |

### Validation Commands

```
./gradlew test           # unit + JPA tests
./gradlew e2eTest        # JBehive BDD stories
./gradlew build          # full build (skips e2eTest in default test task)
npm run build            # frontend build (ui module)
```

### Log Analysis

```
./scripts/analyze_logs.sh --path=./logs
```
