# DONE

Completed steps moved from `task/current/PROGRESS.md` (per iteration convention: active work stays in `PROGRESS.md`, completed work is recorded here).

---

## Step: Fix `:agile:test` failure in `recordingBddExecutionUpdatesStoryStatusAndTimestamp`

### Observed failure
`./gradlew clean build` fails deterministically:
`AgileBoardServiceTest.recordingBddExecutionUpdatesStoryStatusAndTimestamp`
`AssertionFailedError: expected: <true> but was: <false>` at `AgileBoardServiceTest.java:280`
Service log for the run: `BDD verify_coordinate_calculation.story -> PASSED for 0 stories and 0 tasks`

### Root cause
Repository contract mismatch plus fragile test stubbing.

1. `UserStoryRepository.findByBddStory` and `TaskRepository.findByBddStory` return
   `Optional<UserStory>` / `Optional<Task>`, but `AgileBoardService.recordExecution`
   consumes them as collections and reports plural counts in `BddExecutionResponse`.
   `Optional.stream()` yields at most one element, so counts can never exceed one.
   An `Optional`-returning Spring Data derived query also throws
   `IncorrectResultSizeDataAccessException` as soon as two rows match.
2. The test stubs `stories.findByBddStory` twice. Mockito resolves each invocation
   against the latest matching stub, and the generic `any()` matcher overrides the
   earlier specific matcher, so the known BDD story resolves to an empty result.

### Modifications applied
- `agile/.../UserStoryRepository.java` — return `List<UserStory>` from `findByBddStory`; drop unused `Optional` import.
- `agile/.../TaskRepository.java` — return `List<Task>` from `findByBddStory`; drop unused `Optional` import.
- `agile/.../AgileBoardService.java` — iterate the returned lists directly instead of calling `.stream().toList()`.
- `agile/.../AgileBoardServiceTest.java` — replace order-dependent stubs with argument-aware answers that filter the seeded entities by `bddStory`.

### Result
`:agile:test` reports `tests="6" failures="0" errors="0"`; service log reads
`BDD verify_coordinate_calculation.story -> PASSED for 1 stories and 1 tasks`.
The latent `IncorrectResultSizeDataAccessException` is no longer reachable.
A second, unrelated failure then surfaced in `:ui:npmBuild` (next step).

---

## Step: Install `kanban` npm dependencies

### Observed failure
`> Task :ui:npmBuild FAILED`
`[vite]: Rollup failed to resolve import "react/jsx-runtime" from "C:/dev/projects/AI/Psycho/kanban/src/BoardPage.tsx"`

### Root cause
`ui/src/App.tsx` imported `BoardPage` from the sibling Gradle module
(`import BoardPage from '../../kanban/src/BoardPage';`). Module resolution for that
file walked up from `kanban/src`, where no `node_modules` existed. `react` was
installed only in `ui/node_modules`, so `react/jsx-runtime` could not be resolved.
`:rest`, `bdd` and `kanban` never executed because `:ui:npmBuild` halted the build.

### Result (verified 2026-10-05)
`kanban/node_modules` exists (own `package.json`), and `ui/src/App.tsx` no longer
imports from `../../kanban` — `/board` now redirects to the standalone kanban app
via `KANBAN_URL` (`http://localhost:5174`).

---

## Step: Migrate stale `rest` tests to the `AssessmentPayload` scoring API

### Observed failure
`:rest:compileTestJava` failed with 3 errors.
`SemanticMatchingServiceTest.java:29` and `:52`:
`incompatible types: Map<String,Pole> cannot be converted to AssessmentPayload`
`ResultServiceTest.java:91`: `String cannot be converted to Path`

### Root cause
`ArchetypeScoringService.score` now accepts `(List<BipolarQuestion>, AssessmentPayload)`.
`SemanticMatchingServiceTest` still called the superseded overload with a raw
`Map<String, Pole>`. The map is built with `HashMap`, so iteration order was never
guaranteed; `AssessmentPayload.answersFor` resolves answers per axis by question id.

The `ResultServiceTest.java:91` error no longer existed in the working tree — the file
already read `Path.of(Files.readString(...))`, so that report was captured before the
line was corrected. No change was required there.

### Modification applied
`rest/src/test/java/com/unconscious/collective/quiz/SemanticMatchingServiceTest.java`
Express answers as `List<QuizAnswer>` and wrap them in an `AssessmentPayload`, keeping
both test intents and assertions unchanged. `Map` and `HashMap` imports replaced
by `List` and `ArrayList`.

### Result (verified 2026-10-05)
`SemanticMatchingServiceTest` imports `AssessmentPayload` and calls
`scoring.score(bank.all(), new AssessmentPayload(...))` — compiles on the current API.

---

## Step: Fix `@TempDir` initialization order in `ResultServiceTest`

### Observed failure
`:rest:test` failed with `ResultServiceTest.analyzeUsesThePayloadSessionIdAsObjectId`
throwing `java.lang.NullPointerException at ResultServiceTest.java:41`, the
field initializer `new HistoryResult(historyDir.toString(), objectMapper)`.

### Root cause
`@TempDir` was declared on an instance field. JUnit Jupiter creates the test instance
first, running every field initializer, and only afterwards injects the temporary
directory into that field. The `historyResult` and `service` field initializers
therefore dereferenced `historyDir` while it was still `null`.

### Modification applied
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Receive the temporary directory through constructor parameter injection, which JUnit
resolves before field initializers run, and build `historyResult` and `service` from
it inside the constructor. The field is retained so the history assertions can still
read it. No test intent or assertion changes.

### Result
Subsequent runs progressed past instantiation to assertion-level failures (next steps),
confirming the fix.

---

## Step: Read the execution trace JSON as a String

### Observed failure
`:rest:test` ran 11 tests with 1 failure.
`ResultServiceTest.analyzeWritesExecutionTraceJson` threw
`java.nio.file.InvalidPathException at ResultServiceTest.java:94`.

### Root cause
An earlier correction silenced the `String cannot be converted to Path` compile error
by wrapping the file contents in `Path.of(...)`. `Files.readString` already returns a
`String`, and the JSON document contains quotes and newlines that are illegal in a
Windows path, so constructing a `Path` from it always throws. The assertion only ever
needed the text of the file.

### Modification applied
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Hold the trace in a `String` and assert against it directly. No assertion change.

### Result
Failure moved to the next assertion (whitespace-sensitive `sessionId` match).

---

## Step: Assert the trace session id structurally instead of by whitespace


### Observed failure
`ResultServiceTest.analyzeWritesExecutionTraceJson` failed with
`org.opentest4j.AssertionFailedError at ResultServiceTest.java:95`.

### Root cause
`HistoryResult.write` serialises through `writerWithDefaultPrettyPrinter`. Jackson's
`DefaultPrettyPrinter` separates a key from its value with a space before the colon, so
the document contains `"sessionId" : "session"`. The assertion searched for
`"sessionId": "session"` with the space after the colon, which never matches. The test
was asserting on serialiser formatting rather than on the recorded value.

### Modification applied
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Parse the written trace with the existing `objectMapper` and compare the `sessionId`
field, so the assertion is independent of pretty-print layout. The now unused
`assertTrue` import is dropped.

### Result
Test suite green through this step; the investigation then moved to the coordinate
drift itself (next step).

---

## Step: Fix non-deterministic axis coordinates (`task/bug/bug_report_coordinates.md`)

### Observed failure
Identical quiz runs recorded three different X coordinates: `-1.00`, `-0.67`, `-0.33`.
The board `agile/src/main/resources/bugs/coordinates.json` holds the same three runs
(17:24, 17:49, 21:25). Every value is an exact multiple of `1/6` with the per-axis count
pinned at `6`, so the denominator never changed and the numerator drifted by `-2` per
run.

### Root cause
`ArchetypeScoringService.score` is a pure function of `(questions, answers)`, and
`AssessmentPayload.answersFor` already filters null poles and de-duplicates by
`questionId`, so the arithmetic itself cannot drift. The drift came from the *input*:
`QuestionBank` had been changed from immutable to mutable and shared, and
`POST /api/analysis/questions` appended questions at runtime. The set rendered by
`GET /api/analysis/questions` therefore differed between runs, so the same click
sequence bound poles to different question IDs and the X sum moved while the count
stayed at 6.

### Modification applied
- `rest/.../service/QuestionBank.java`
  Restored the documented immutable invariant: `final` field assigned once through
  `List.copyOf(load(...))`, dropped `add`/`addAll`/`validate`, and reject duplicate IDs in
  `load` so the scoring `questionAxisMap` can never silently last-write-wins an axis.
- `rest/.../service/QuizService.java`
  Removed `addQuestions`, the only caller of `questionBank.addAll`.
- `rest/.../controller/AnalysisController.java`
  Removed `POST /api/analysis/questions`, its `validateQuestions` helper and the now
  unused `BipolarQuestion` import. `GET /api/analysis/questions` is unchanged.
- `rest/src/test/.../ArchetypeScoringServiceDeterminismTest.java`
  Locked the fix with duplicate-ID and immutability regression tests.

### Result (verified 2026-10-05)
`QuestionBank` holds `private final List<BipolarQuestion> questions` assigned via
`List.copyOf(load(objectMapper))` with `requireUniqueIds(loaded)` called in `load`, and
`ArchetypeScoringServiceDeterminismTest` exists with the duplicate/null-pole regression
tests. The question set is frozen for the lifetime of the process, so `score` receives
the same arguments on every run and coordinates are reproducible.

---

## Completed Phases (of the original Phases & Planned Modifications)

### Phase 0 — Bug Fix ✅
- `ArchetypeScoringService.score()` isolates all accumulators as locals, 2-decimal rounding, null-pole handling, dedup by question ID.
- `AssessmentPayload.answersFor()` skips null poles and deduplicates.
- `ResultService` delegates scoring to `ArchetypeScoringService` (no manual HashMap loop).
- `ArchetypeScoringServiceDeterminismTest` added — verified present.

### Phase 1 — Wire HistoryResult into ResultService ✅
- `HistoryResult` injected into `ResultService` constructor — verified: field
  `private final HistoryResult historyResult` present, written on every analyze call.
- `task/current/artifact_pi.md` Commit 1 records the change.

### Phase 3 — JBehave BDD + E2E ✅ (as executed; original file paths superseded)
- `org.jbehave:jbehave-core:5.2.0` (not 5.1.0) added to **`qa/build.gradle`** (module renamed from `bdd`).
- Gradle task `e2eTest` (JUnit Platform, `**/*E2ETest.class`) — verified in `qa/build.gradle`.
- Story: **`qa/src/main/resources/stories/verify_coordinate_calculation.story`**.
- Steps: `qa/.../ui/steps/CoordinateCalculationSteps.java`, `HistoryStep.java`.
- Locators/POM: `qa/.../ui/pages/Locators.java`, `AxisControlsPage`, `DashboardPage`.

### Phase 5 — Agile domain persistence ✅ (as executed; original plan superseded)
- Implemented as the standalone **`agile` Gradle module** instead of a `bdd` library wired into `rest`.
- All six entities + repos + `AgileController` + `AgileBoardService` + H2/Postgres/JSON configs + seed data verified present.

### Phase 6 — Kanban UI ✅ (corrected path)
- Board lives in the **`kanban/`** module (not `ui/src/kanban/`): `BoardPage`, `BoardColumn`,
  `KanbanCard`, `DetailDrawer`, `BddStatusBadge`.
- `ui/src/App.tsx` routes `/board` → `KANBAN_URL` (:5174) redirect.

---

## Step: Create `task/current/impl_pi.md` (implementation master document)

### Planned modifications
- `task/current/impl_pi.md` (new file): verified status matrix of all six `BDD.md`
  spec sections, authoritative decisions resolving the documentation conflicts
  (spec filenames verbatim, `/result/history/` as history path, module map
  `qa`/`agile`/`kanban`/`rest`, `jbehave-core:5.2.0`, `ScoringService` =
  `ArchetypeScoringService`), ordered implementation steps for all remaining work,
  validation commands, and a RULES.md compliance checklist.
- `task/current/PROGRESS.md` — pre-edit log entry (RULES §5).
- `task/current/artifact_pi.md` — conventional commit message artifact (RULES §5).

### Result
File created at `task/current/impl_pi.md`; supersedes the conflicting plan blocks in
`draft.md` and the stale checklist in `review.md` without modifying those files.

---

## Step: Move completed steps from PROGRESS.md to DONE.md

### Planned modifications
- `task/current/DONE.md` — receive every completed step (this file).
- `task/current/PROGRESS.md` — rewrite to pending work only (spec §4 artifact,
  Phase 7 validation, TODO items → `impl_pi.md` Steps 1–10); also removes the
  duplicated `- \`npm run build\`` line and the duplicated paragraph in the
  session-id step.
- `task/current/artifact_pi.md` — conventional commit message artifacts.

### Result
`PROGRESS.md` now holds active work only; all completed steps (7 fix steps,
Phases 0/1/3/5/6, both documentation steps) are recorded in this file. No content lost.

---

## Step: Save BDD correction plan to `task/current/plan.md`

### Planned modifications
- `task/current/plan.md` (new file): full plan for "impl correct bdd" — five verified
  defects in the `qa` module (missing `*E2ETest` runner, duplicate steps in
  `HistoryStep`, wrong trace assertion, missing `QuizViewPage`, missing spec §1
  scenarios) plus the ordered edit list and validation commands.
- `task/current/PROGRESS.md` — pre-edit log entry (RULES §5).
- `task/current/artifact_pi.md` — commit-message artifact (Commit 8).

### Result
`task/current/plan.md` created with sections: verified defects + evidence table,
ordered edit list (8 steps), validation commands, risks/assumptions. No `qa`
source files touched — awaiting approval.

