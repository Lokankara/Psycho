# plan.md — "impl correct bdd" (BDD Correction Plan)

**Created:** 2026-10-05
**Scope:** make the BDD implementation in the `qa` module correct per `task/current/BDD.md` §1–§3 and
`task/bdd_specifications.md`.
**Status:** PLAN ONLY — no `qa` source files have been edited yet. Awaiting approval.

---

## 1. Verified Defects (evidence from repository inspection)

| # | Defect                                                                                                                                                                                                                                                                                                                                                                     | Evidence                                                                                                    |
|---|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| 1 | **E2E runner does not exist** — `e2eTest` includes `**/*E2ETest.class` but no such class exists anywhere in `qa/src`, so `./gradlew :qa:e2eTest` runs **0 tests**. `artifact_pi.md` Commit 3 and `bdd_specifications.md` claim `VerifyCoordinateCalculationE2ETest` exists — it does not.                                                                                  | `qa/build.gradle:32`; no `*E2ETest` file in `qa/src`                                                        |
| 2 | **`HistoryStep` duplicates every step** of `CoordinateCalculationSteps` (identical `@Given/@When/@Then` strings) → JBehave reports ambiguous/duplicate steps when both classes are registered as candidates.                                                                                                                                                               | `qa/.../ui/steps/HistoryStep.java` vs `CoordinateCalculationSteps.java`                                     |
| 3 | **Trace assertion is wrong** — `thenTraceFilesWritten()` scans `java.io.tmpdir` for *any* `result_*.json` and only asserts the list is non-empty; it never verifies the traces written by this run, never checks payload-hash equality ⇒ identical coordinates (the spec §2 acceptance), and `HistoryStep`'s temp `historyDirectory` is created but written to by nothing. | `HistoryStep.java:39-57, 151-166`                                                                           |
| 4 | **`QuizViewPage` missing** — spec §1, `AC.md:15` and `task/bdd_specifications.md` §Page Objects all document it; only `DashboardPage` and `AxisControlsPage` exist.                                                                                                                                                                                                        | `qa/.../ui/pages/` contains no `QuizViewPage`                                                               |
| 5 | **Story gaps vs spec §1** — §1 requires steps covering *full quiz completion*, *coordinate generation* and *archetype matching*; the story has only the 4 determinism scenarios, and `ArchetypeScoringService.score()` returns `matches = List.of()` (javadoc: "and no matches"), so archetype matching must go through `SemanticMatchingService.match(profile)`.          | `verify_coordinate_calculation.story`; `ArchetypeScoringService.java:68`; `SemanticMatchingService.java:39` |

Secondary (documentation drift, fixed in the same pass): `task/bdd_specifications.md` still points at `bdd/src/test/...`
paths and packages `com.unconscious.collective.qa.steps/.pages` — actual location is `qa/src/main/...` with packages
`com.unconscious.collective.qa.ui.steps` / `.ui.pages`; it references a `Locators.QUIZ_QUESTION_BY_INDEX` field while
the code names it `QUIZ_QUESTION`.

## 2. Ordered Edit List

1. **`task/current/PROGRESS.md`** — pre-edit step log (RULES §5). *(done for this plan step)*
2. **Create `qa/src/main/java/com/unconscious/collective/qa/VerifyCoordinateCalculationE2ETest.java`**
    - JUnit 5 `@Test` that drives the JBehave `Embedder` (documented design: `InstanceStepsFactory`, story path
      `stories/verify_coordinate_calculation.story`, reporter STDOUT + CONSOLE + TXT,
      `doGenerateViewAfterStories(false)` to avoid report-view generation).
    - Registers **only** `CoordinateCalculationSteps` as candidate steps.
3. **Rewrite `qa/.../ui/steps/CoordinateCalculationSteps.java`** (single source of step truth):
    - Keep the 4 existing scenarios' steps unchanged (story compatibility).
    - Add `HistoryResult` (constructor arg `../result/history`, i.e. spec §2 path) and record every
      `write(ExecutionTrace.of(payload, profile))` path from `whenScored()` / `whenScoredTwice()`.
    - Move the trace step here as `thenTraceFilesWritten()`: assert **2 files** written **by this run**, parse both
      JSONs, assert equal `payloadHash` ⇒ equal `x/y/z/octant` (spec §2 acceptance).
    - New scenario A — **full quiz completion / coordinate generation**: Given every question of the quiz is answered /
      When the assessment is scored / Then X, Y, Z are finite and within [-1.0, 1.0] and every axis count > 0.
    - New scenario B — **archetype matching**: Given an identical set of quiz answers / When the assessment is scored
      and matched / Then the octant has a primary archetype match (via `SemanticMatchingService.match`).
4. **Delete `qa/.../ui/steps/HistoryStep.java`** — duplicate of defect 2/3; it is absent from the
   `bdd_specifications.md` step catalog, so the catalog authoritatively belongs to `CoordinateCalculationSteps`.
5. **Create `qa/.../ui/pages/QuizViewPage.java`** — final/stateless, static getters delegating to
   `Locators.QUIZ_QUESTION`, `QUIZ_NEGATIVE_BUTTON`, `QUIZ_POSITIVE_BUTTON`, `QUIZ_NEXT_BUTTON`, `QUIZ_SUBMIT_BUTTON` (
   methods: `questionLocator()`, `negativeButtonLocator()`, `positiveButtonLocator()`, `nextButtonLocator()`,
   `submitButtonLocator()`).
6. **Extend `qa/src/main/resources/stories/verify_coordinate_calculation.story`** with the two new scenarios (wording
   must match the new step strings exactly).
7. **Update `task/bdd_specifications.md`** — correct paths (`qa/src/main/resources/stories/`,
   `qa/src/main/java/.../ui/steps|pages/`), correct packages, add the two new scenario rows and the two new step rows,
   fix `QUIZ_QUESTION_BY_INDEX` → `QUIZ_QUESTION`, runner task = `qa` module.
8. **`task/current/artifact_pi.md`** — append conventional commit-message artifact (RULES §5).

Out of scope (tracked in `impl_pi.md` Steps 7–8): `verify_board_interaction.story` / board locators execution and the
Playwright-vs-Selenium driver decision — these need a browser-driver decision first; this plan keeps the existing
in-process (service-level) E2E style.

---

## 3. Validation (user/CI runs — agent does not execute Gradle)

```
.\gradlew.bat :qa:compileJava     # new runner + steps + QuizViewPage compile
.\gradlew.bat :qa:e2eTest         # must report 6 scenarios, all passing
```

**Expected:** `e2eTest` executes 6 scenarios (4 existing + 2 new), two fresh `result/history/result_*.json` files appear
per run of scenario 1, no ambiguous-step errors, no `//` comments introduced (RULES §3), no `git` commands run (RULES
§2).

---

## 4. Risks / Assumptions

- JBehave 5.2.0 `Embedder` API is used from memory (`useConfiguration`, `useCandidateSteps`, `runStoriesAsPaths`); if a
  signature differs, `:qa:compileJava` surfaces it immediately — fallback is `MostUsefulConfiguration` +
  `InstanceStepsFactory` variants.
- Traces land in `../result/history` relative to the `qa` working directory (Gradle default = module dir) → resolves to
  repo-root `result/history` ✓ matches spec §2.
- JUnit Jupiter availability on the `qa` classpath was not verifiable without running Gradle (rejected); if
  `:qa:compileJava` fails on missing `org.junit.jupiter`, add `implementation 'org.junit.jupiter:junit-jupiter'` (
  version managed by the Spring Boot BOM, same as existing `org.slf4j:slf4j-api`).

