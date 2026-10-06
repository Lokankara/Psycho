# Bug Report: Non-Deterministic Axis Coordinates Calculation

## Status

CONFIRMED / REPRODUCED

## Observation Log

- Run 1 (20:55:23): (-1.00, -1.00, -1.00) -> Octant: MASTER_PRAGMATIST
- Run 2 (20:55:46): (-0.67, -1.00, -1.00) -> Octant: MASTER_PRAGMATIST

## Impact

Coordinates drift between identical or near-identical submissions while maintaining the same Octant, breaking
calculation determinism and E2E test assertions.

## Root Cause Analysis

1. Unfiltered `null` poles in `AssessmentPayload` changing the denominator count `N`.
2. Missing deduplication per `questionId` allowing duplicate answer payloads to inflate `N`.
3. Lack of explicit 2-decimal rounding (`Math.round(val * 100.0) / 100.0`) on raw double division results.

## Fix Verification Strategy

1. Enforce single calculation path in `ArchetypeScoringService.score()`.
2. Filter `chosenPole != null` in `AssessmentPayload.answersFor()`.
3. Apply 2-decimal rounding to final $X, Y, Z$ coordinates.

## Resolution

All three fixes are present in the current source. Exact locations:

| Root cause                            | Fix location                                                                                                                                                                 | Status  |
|---------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------|
| Unfiltered `null` poles inflating `N` | `AssessmentPayload.answersFor()` skips `chosenPole == null` (line 36)                                                                                                        | APPLIED |
| Duplicate `questionId`s inflating `N` | `AssessmentPayload.answersFor()` dedups via `LinkedHashMap`, last non-null wins (line 39)                                                                                    | APPLIED |
| Missing 2-decimal rounding            | `ArchetypeScoringService.normalize()` -> `Math.round(mean * 100.0) / 100.0` (line 80)                                                                                        | APPLIED |
| Divergent scoring paths               | `ResultService.analyze` / `analyzeAndSave` delegate to `ArchetypeScoringService.score()` (lines 73 / 84); `QuizService.evaluate` builds an `AssessmentPayload` (lines 39-46) | APPLIED |

Fully-qualified fix paths:

- `rest/src/main/java/com/unconscious/collective/quiz/service/ArchetypeScoringService.java` (`score()` lines 41-69,
  `normalize()` line 80)
- `rest/src/main/java/com/unconscious/collective/quiz/domain/dto/AssessmentPayload.java` (`answersFor()` lines 32-42)
- `rest/src/main/java/com/unconscious/collective/quiz/domain/value/Coordinates.java` (`of()` lines 12-14, clamp)

Determinism: each coordinate is `sum / count`, where `count` is the number of distinct, non-null answers for the axis,
then rounded to 2 decimals. For the 6 X-axis questions, `X = -1.00` requires 6/6 `NEGATIVE`, while `X = -0.67` requires
`-4/6`; identical payloads can no longer produce different coordinates.

Regression test: `rest/src/test/java/com/unconscious/collective/quiz/ArchetypeScoringServiceDeterminismTest.java`.

## Verification

Pending command-run (agent must not execute `gradlew` per `task/RULES.md` section 2):

```powershell
.\gradlew.bat :rest:test --tests "com.unconscious.collective.quiz.ArchetypeScoringServiceDeterminismTest" -x :ui:npmBuild
```
