# User Story: Deterministic Semantic Coordinate Calculation

## User Story

**As a** semantic archetype analyzer,
**I want** coordinate calculation to be fully deterministic for identical answer sets,
**so that** every run with the same answers produces the same X, Y, Z coordinates
and the same matched Octant without drift.

## Acceptance Criteria (Given-When-Then)

### AC-1: Identical payloads yield identical coordinates

| Step      | Condition                                                                                  |
|-----------|--------------------------------------------------------------------------------------------|
| **Given** | a session answer set where every question has a pole chosen                                |
| **When**  | the `ArchetypeScoringService.score()` method is called twice with the same answers         |
| **Then**  | both results have identical `x`, `y`, and `z` values (within `1e-9`) and the same `Octant` |

### AC-2: Execution trace is persisted as JSON

| Step      | Condition                                                                                                               |
|-----------|-------------------------------------------------------------------------------------------------------------------------|
| **Given** | a completed assessment with a valid `AssessmentPayload`                                                                 |
| **When**  | `ResultService.analyzeAndSave()` processes the payload                                                                  |
| **Then**  | a file named `result_YYYYMMDD_HHMMSS.json` exists under `task/history/`                                                 |
| **And**   | the JSON contains `sessionId`, `payloadHash`, `axisCounts`, `axisSums`, `x`, `y`, `z`, `octant`, and `timestamp` fields |
| **And**   | identical payload hashes always produce identical `x/y/z/octant` values                                                 |

### AC-3: Null pole choices are skipped

| Step      | Condition                                                      |
|-----------|----------------------------------------------------------------|
| **Given** | an assessment where one question has a `null` pole choice      |
| **When**  | the assessment is scored                                       |
| **Then**  | no `NullPointerException` is thrown                            |
| **And**   | the null-pole question is excluded from the axis count and sum |
| **And**   | the coordinate is computed from answered questions only        |

### AC-4: Duplicate question IDs use last non-null pole

| Step      | Condition                                                            |
|-----------|----------------------------------------------------------------------|
| **Given** | an assessment with duplicate answer entries for the same question ID |
| **When**  | the assessment is scored                                             |
| **Then**  | the last non-null pole for each question ID is used                  |
| **And**   | the answer count for the affected axis is not inflated               |

### AC-5: Unanswered axes score zero

| Step      | Condition                                                        |
|-----------|------------------------------------------------------------------|
| **Given** | an assessment where no questions are answered for a given axis   |
| **When**  | the assessment is scored                                         |
| **Then**  | the coordinate for that axis is `0.0`                            |
| **And**   | the resolved Octant treats zero as the positive side of the axis |

### AC-6: Coordinates are clamped to [-1.0, 1.0]

| Step      | Condition                                                |
|-----------|----------------------------------------------------------|
| **Given** | any valid answer set                                     |
| **When**  | the assessment is scored                                 |
| **Then**  | all three coordinates are within the range `[-1.0, 1.0]` |

### AC-7: Coordinates are rounded to two decimal places

| Step      | Condition                                                                         |
|-----------|-----------------------------------------------------------------------------------|
| **Given** | any valid answer set                                                              |
| **When**  | the assessment is scored                                                          |
| **Then**  | each coordinate is a multiple of `0.01` (i.e., `Math.round(val * 100.0) / 100.0`) |

### AC-8: E2E coordinate determinism story

| Step      | Condition                                                    |
|-----------|--------------------------------------------------------------|
| **Given** | a verified scenario in `verify_coordinate_calculation.story` |
| **When**  | the JBehive story is executed via the `e2eTest` Gradle task  |
| **Then**  | all scenarios pass and the build is green                    |
| **And**   | execution trace JSON files are written to `task/history/`    |

## Traceability

| Acceptance Criterion | Test                                                                             |
|----------------------|----------------------------------------------------------------------------------|
| AC-1                 | `ArchetypeScoringServiceTest` + JBehive `verify_coordinate_calculation.story`    |
| AC-2                 | `ResultServiceTest.analyzeWritesExecutionTraceJson`                              |
| AC-3                 | `ResultServiceTest.analyzeSkipsAnswersWithUnknownPole`                           |
| AC-4                 | `BddVerifyDuplicateAnswers` (JBehive scenario)                                   |
| AC-5                 | `ArchetypeScoringServiceTest.emptyAnswersProduceZeroCoordinatesAndDefaultOctant` |
| AC-6                 | `Vector3D` clamping behavior + `Coordinates.of()`                                |
| AC-7                 | `ArchetypeScoringServiceTest.balancedAnswersOnAnAxisCancelOut`                   |
| AC-8                 | `VerifyCoordinateCalculationE2ETest` (e2eTest task)                              |
