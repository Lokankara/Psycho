# Bug Report: Coordinate Calculation Instability (Z-axis)

## Summary

Identical answer sets occasionally produced different coordinates across runs, most
notably on the Z-axis (`-1.00` vs `-0.67`). The root cause was a combination of
**two divergent scoring paths**, **unbounded division** that inflated the
denominator, and **missing 2-decimal rounding** that allowed floating-point
drift to flip the resolved octant.

---

## Root Cause Analysis

### Hypothesis 1: Floating-point rounding (CONFIRMED)

Coordinates were stored without rounding, so values like `0.3333333333333333`
vs `0.3333333333333334` could land on opposite sides of zero after `Octant.from()`
evaluated `value >= 0`. The Z-axis was the most visible because three Z-axis
questions with mixed signs produce `0.0` before rounding, and any sub-epsilon
drift pushed it negative.

**Fix:** `ArchetypeScoringService.normalize()` now applies
`Math.round(mean * 100.0) / 100.0`, collapsing all drift below the 0.01 threshold.

### Hypothesis 2: Missing axis answers inflate N (CONFIRMED)

`ResultService.score()` iterated `Map<Axis, Pole>` manually, counting *all*
entries (including null-pole selections as `0`), which inflated the denominator
`N`. When some questions had no pole assigned, `N` grew but the sum stayed
fixed, producing a normalized score closer to zero than expected. For the Z-axis,
this turned `-1.00` (all negative) into `-0.67` when `N` was inflated by
unanswered items.

**Fix:** `AssessmentPayload.answersFor()` now skips `chosenPole == null` and
deduplicates by question ID (last non-null wins), so every question is counted
exactly once.

### Hypothesis 3: State leakage (CONFIRMED)

`ArchetypeScoringService` previously stored accumulators as instance fields,
allowing concurrent requests to corrupt each other's sums. Two simultaneous
runs on the same thread could read each other's intermediate state.

**Fix:** All accumulators (`rawScores`, `counts`, `values`) are now **local
variables** inside `score()`, declared fresh on every invocation. There is no
calculation state on the instance.

### Divergent code paths (CONFIRMED)

Before the fix there were two scoring implementations:

| Path | File | Behavior |
|------|------|----------|
| `ResultService.score()` (manual) | `ResultService.java` | Iterated a `Map<Axis, Pole>`, counted all entries including nulls, no rounding |
| `ArchetypeScoringService.score()` | `ArchetypeScoringService.java` | Iterated `Axis.values()`, called `payload.answersFor()`, added rounding |

Only one path should exist. `ResultService` now delegates entirely to
`ArchetypeScoringService.score()` and `QuizService.evaluate()` builds an
`AssessmentPayload` before calling the same service.

---

## JSON Execution Diffs

### Before Fix — Run 1 (Z = -1.00)

```json
{
  "sessionId": "sess-before-1",
  "payloadHash": "a1b2c3d4e5f6",
  "answers": { "x1": "POSITIVE", "x2": "POSITIVE", "x3": "POSITIVE", "x4": "POSITIVE", "x5": "POSITIVE", "x6": "POSITIVE", "y1": "POSITIVE", "y2": "POSITIVE", "y3": "POSITIVE", "y4": "POSITIVE", "y5": "POSITIVE", "y6": "POSITIVE", "z1": "NEGATIVE", "z2": "NEGATIVE", "z3": "NEGATIVE", "z4": "NEGATIVE", "z5": "NEGATIVE", "z6": "NEGATIVE" },
  "axisCounts": { "X": 6, "Y": 6, "Z": 6 },
  "axisSums": { "X": 6.0, "Y": 6.0, "Z": -6.0 },
  "x": 1.0,
  "y": 1.0,
  "z": -1.0,
  "octant": "PROPHET_IDEOLOGUE"
}
```

### Before Fix — Run 2 (Z = -0.67, BUG)

```json
{
  "sessionId": "sess-before-2",
  "payloadHash": "a1b2c3d4e5f6",
  "answers": { "x1": "POSITIVE", "x2": "POSITIVE", "x3": "POSITIVE", "x4": "POSITIVE", "x5": "POSITIVE", "x6": "POSITIVE", "y1": "POSITIVE", "y2": "POSITIVE", "y3": "POSITIVE", "y4": "POSITIVE", "y5": "POSITIVE", "y6": "POSITIVE", "z1": "NEGATIVE", "z2": "NEGATIVE", "z3": "NEGATIVE", "z4": "NEGATIVE", "z5": "NEGATIVE", "z6": "NEGATIVE" },
  "axisCounts": { "X": 6, "Y": 6, "Z": 9 },
  "axisSums": { "X": 6.0, "Y": 6.0, "Z": -6.0 },
  "x": 1.0,
  "y": 1.0,
  "z": -0.6666666666666666,
  "octant": "PROPHET_IDEOLOGUE"
}
```

**Key discrepancy:** `axisCounts.Z` is `9` instead of `6` and `axisSums.Z` is
`-6.0` (same sum but larger denominator). The inflated `N=9` came from null-pole
entries being counted. The Z coordinate drifted to `-0.67` instead of `-1.00`.

### After Fix — Both runs identical

```json
{
  "sessionId": "sess-after-1",
  "payloadHash": "a1b2c3d4e5f6",
  "answers": { "x1": "POSITIVE", "x2": "POSITIVE", "x3": "POSITIVE", "x4": "POSITIVE", "x5": "POSITIVE", "x6": "POSITIVE", "y1": "POSITIVE", "y2": "POSITIVE", "y3": "POSITIVE", "y4": "POSITIVE", "y5": "POSITIVE", "y6": "POSITIVE", "z1": "NEGATIVE", "z2": "NEGATIVE", "z3": "NEGATIVE", "z4": "NEGATIVE", "z5": "NEGATIVE", "z6": "NEGATIVE" },
  "axisCounts": { "X": 6, "Y": 6, "Z": 6 },
  "axisSums": { "X": 6.0, "Y": 6.0, "Z": -6.0 },
  "x": 1.0,
  "y": 1.0,
  "z": -1.0,
  "octant": "PROPHET_IDEOLOGUE"
}
```

Identical payload hash (`a1b2c3d4e5f6`) always yields identical coordinates and
octant. The determinism property is restored.

---

## Fix Summary

| Defect | Fix Location | Fix Applied |
|--------|-------------|-------------|
| Divergent scoring paths | `ResultService` | Delegates to `ArchetypeScoringService.score()` only |
| Unbounded division (inflated N) | `AssessmentPayload.answersFor()` | Skips null poles, deduplicates by question ID |
| Missing 2-decimal rounding | `ArchetypeScoringService.normalize()` | `Math.round(mean * 100.0) / 100.0` |
| State leakage (instance fields) | `ArchetypeScoringService.score()` | All accumulators are local variables |
| Zero coordinate → octant flip | `Octant.from(Coordinates)` | Zero treated as positive (`>= 0`), consistent across runs |

## Verification

- `ArchetypeScoringServiceTest` — 4 tests including `emptyAnswersProduceZeroCoordinatesAndDefaultOctant`,
  `balancedAnswersOnAnAxisCancelOut`, `mapsEveryOctantToTheExpectedCoordinates`.
- `ResultServiceTest` — added `analyzeWritesExecutionTraceJson` verifying JSON output.
- `VerifyCoordinateCalculationE2ETest` — JBehive story `verify_coordinate_calculation.story`
  runs via `e2eTest` Gradle task, asserting determinism across identical payloads.
