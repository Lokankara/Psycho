# BDD Specifications: JBehive Step ↔ Page ↔ Locator Catalog

## Overview

JBehive BDD stories live in `bdd/src/test/resources/stories/*.story`. Step
definitions are in `bdd/src/test/java/com/unconscious/collective/bdd/steps/`.
Page Object Models and the centralized `Locators` registry are in
`bdd/src/test/java/com/unconscious/collective/bdd/pages/`.

## Story Files

### verify_coordinate_calculation.story

| Scenario                                                 | Purpose                |
|----------------------------------------------------------|------------------------|
| Identical positive answers produce identical coordinates | Determinism regression |
| Balanced answers on an axis cancel to zero               | Axis cancellation      |
| Null pole choices are skipped without error              | Null safety            |
| Duplicate question IDs use last non-null pole            | Dedup correctness      |

---

## Step Definitions

`CoordinateCalculationSteps.java` — `com.unconscious.collective.qa.steps`

| JBehive Step                                                                              | Implementation Method                    |
|-------------------------------------------------------------------------------------------|------------------------------------------|
| `Given an identical set of quiz answers`                                                  | `givenIdenticalAnswerSet()`              |
| `Given a balanced set of answers for the Z axis`                                          | `givenBalancedZAnswers()`                |
| `Given an assessment with a null pole choice on one question`                             | `givenNullPoleChoice()`                  |
| `Given a duplicate answer for the same question ID`                                       | `givenDuplicateAnswer()`                 |
| `When the assessment is scored twice`                                                     | `whenScoredTwice()`                      |
| `When the assessment is scored`                                                           | `whenScored()`                           |
| `Then both runs return identical X, Y, Z coordinates and the same Octant`                 | `thenCoordinatesAreIdentical()`          |
| `Then the Z coordinate is 0.0 and the Octant is PROPHET_IDEOLOGUE`                        | `thenZCoordinateIsZero()`                |
| `Then no exception is thrown and the coordinate is computed from answered questions only` | `thenNoExceptionAndCoordinateComputed()` |
| `Then the last non-null pole is used and the coordinate count is not inflated`            | `thenLastNonNullPoleUsed()`              |
| `Then both runs log an execution trace JSON file`                                         | `thenTraceFilesWritten()`                |

---

## Page Object Models

All POM classes are final, stateless, and expose static locator getter methods
that delegate to `Locators`. No raw CSS/XPath string literals appear in step
definitions.

### QuizViewPage

| Method                    | Returns                           |
|---------------------------|-----------------------------------|
| `questionLocator()`       | `Locators.QUIZ_QUESTION_BY_INDEX` |
| `negativeButtonLocator()` | `Locators.QUIZ_NEGATIVE_BUTTON`   |
| `positiveButtonLocator()` | `Locators.QUIZ_POSITIVE_BUTTON`   |
| `nextButtonLocator()`     | `Locators.QUIZ_NEXT_BUTTON`       |
| `submitButtonLocator()`   | `Locators.QUIZ_SUBMIT_BUTTON`     |

### DashboardPage

| Method                     | Returns                               |
|----------------------------|---------------------------------------|
| `titleLocator()`           | `Locators.DASHBOARD_TITLE`            |
| `octantBadgeLocator()`     | `Locators.DASHBOARD_OCTANT_BADGE`     |
| `coordinateLabelLocator()` | `Locators.DASHBOARD_COORDINATE_LABEL` |
| `historyLinkLocator()`     | `Locators.DASHBOARD_HISTORY_LINK`     |

### AxisControlsPage

| Method               | Returns               |
|----------------------|-----------------------|
| `axisBarLocator()`   | `Locators.AXIS_BAR`   |
| `axisXBarLocator()`  | `Locators.AXIS_X_BAR` |
| `axisYBarLocator()`  | `Locators.AXIS_Y_BAR` |
| `axisZBarLocator()`  | `Locators.AXIS_Z_BAR` |
| `axisValueLocator()` | `Locators.AXIS_VALUE` |

---

## Locators Registry

`Locators.java` — `com.unconscious.collective.qa.pages`

### Quiz View Locators

| Field                    | Selector                            |
|--------------------------|-------------------------------------|
| `QUIZ_QUESTION_BY_INDEX` | `div[data-testid="quiz-question"]`  |
| `QUIZ_NEGATIVE_BUTTON`   | `button[data-pole="NEGATIVE"]`      |
| `QUIZ_POSITIVE_BUTTON`   | `button[data-pole="POSITIVE"]`      |
| `QUIZ_NEXT_BUTTON`       | `button[data-testid="quiz-next"]`   |
| `QUIZ_SUBMIT_BUTTON`     | `button[data-testid="quiz-submit"]` |

### Dashboard Locators

| Field                        | Selector                               |
|------------------------------|----------------------------------------|
| `DASHBOARD_TITLE`            | `h1`                                   |
| `DASHBOARD_OCTANT_BADGE`     | `span[data-testid="octant-badge"]`     |
| `DASHBOARD_COORDINATE_LABEL` | `span[data-testid="coordinate-label"]` |
| `DASHBOARD_HISTORY_LINK`     | `a[href="/history"]`                   |

### Axis Control Locators

| Field        | Selector                        |
|--------------|---------------------------------|
| `AXIS_BAR`   | `div[data-testid="axis-bar"]`   |
| `AXIS_X_BAR` | `div[data-axis="X"]`            |
| `AXIS_Y_BAR` | `div[data-axis="Y"]`            |
| `AXIS_Z_BAR` | `div[data-axis="Z"]`            |
| `AXIS_VALUE` | `div[data-testid="axis-value"]` |

### Kanban Board Locators

| Field                   | Selector                                |
|-------------------------|-----------------------------------------|
| `BOARD_CONTAINER`       | `section[data-testid="kanban-board"]`   |
| `BOARD_COLUMN`          | `section[aria-label$="column"]`         |
| `BOARD_CARD`            | `article[data-testid^="kanban-card"]`   |
| `BOARD_PI_SELECTOR`     | `select[data-testid="pi-selector"]`     |
| `BOARD_SPRINT_SELECTOR` | `select[data-testid="sprint-selector"]` |

---

## Test Runner

`VerifyCoordinateCalculationE2ETest.java` — `com.unconscious.collective.qa`

| Aspect            | Detail                                                                  |
|-------------------|-------------------------------------------------------------------------|
| Framework         | JBehive `Embedder` invoked from JUnit 5 `@Test`                         |
| Task              | Gradle `e2eTest` (`bdd` module)                                         |
| Story path        | `**/verify_coordinate_calculation.story`                                |
| Step factory      | `InstanceStepsFactory` with `CoordinateCalculationSteps`                |
| Reporter          | STDOUT + CONSOLE + TXT                                                  |
| Naming convention | Classes matching `*E2ETest` are picked up by `e2eTest` only, not `test` |