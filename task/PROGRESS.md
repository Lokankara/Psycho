# PROGRESS

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

### Planned modifications
- `agile/src/main/java/com/unconscious/collective/agile/UserStoryRepository.java`
  Return `List<UserStory>` from `findByBddStory`; drop the now unused `Optional` import.
- `agile/src/main/java/com/unconscious/collective/agile/TaskRepository.java`
  Return `List<Task>` from `findByBddStory`; drop the now unused `Optional` import.
- `agile/src/main/java/com/unconscious/collective/agile/AgileBoardService.java`
  Iterate the returned lists directly instead of calling `.stream().toList()`.
- `agile/src/test/java/com/unconscious/collective/agile/AgileBoardServiceTest.java`
  Replace the order-dependent stubs with argument-aware answers that filter the seeded
  entities by `bddStory`, mirroring real repository behaviour for both stories and tasks.

### Expected outcome
`:agile:test` passes and the full multi-module build completes, with the latent
`IncorrectResultSizeDataAccessException` no longer reachable.

### Result of the above step
`:agile:test` now reports `tests="6" failures="0" errors="0"` and the service log reads
`BDD verify_coordinate_calculation.story -> PASSED for 1 stories and 1 tasks`.
Both the story and the task are now updated, which the previous `Optional` contract
made structurally impossible.

The build remained red because a second, unrelated failure surfaced in `:ui:npmBuild`
once `:agile:test` stopped blocking execution.

## Step: Install `kanban` npm dependencies

### Observed failure
`> Task :ui:npmBuild FAILED`
`[vite]: Rollup failed to resolve import "react/jsx-runtime" from "C:/dev/projects/AI/Psycho/kanban/src/BoardPage.tsx"`

### Root cause
`ui/src/App.tsx` imports `BoardPage` from the sibling Gradle module:
`import BoardPage from '../../kanban/src/BoardPage';`
Module resolution for that file walks up from `kanban/src`, where no `node_modules`
exists. `react` is installed only in `ui/node_modules`, so `react/jsx-runtime` cannot
be resolved from the kanban source tree.
`:rest`, `:bdd` and `:kanban` never executed because `:ui:npmBuild` halted the build.

## Step: Migrate stale `rest` tests to the `AssessmentPayload` scoring API

### Observed failure
`:rest:compileTestJava` failed with 3 errors.
`SemanticMatchingServiceTest.java:29` and `:52`:
`incompatible types: Map<String,Pole> cannot be converted to AssessmentPayload`
`ResultServiceTest.java:91`: `String cannot be converted to Path`

### Root cause
`ArchetypeScoringService.score` now accepts `(List<BipolarQuestion>, AssessmentPayload)`.
`SemanticMatchingServiceTest` still calls the superseded overload with a raw
`Map<String, Pole>`. The map is built with `HashMap`, so iteration order was never
guaranteed; `AssessmentPayload.answersFor` resolves answers per axis by question id.

The `ResultServiceTest.java:91` error no longer exists in the working tree. The file
already reads `Path.of(Files.readString(...))`, so that report was captured before the
line was corrected. No change is required there.

### Planned modification
`rest/src/test/java/com/unconscious/collective/quiz/SemanticMatchingServiceTest.java`
Express answers as `List<QuizAnswer>` and wrap them in an `AssessmentPayload`, keeping
both test intents and assertions unchanged. `Map` and `HashMap` imports are replaced
by `List` and `ArrayList`.

## Step: Fix `@TempDir` initialization order in `ResultServiceTest`

### Observed failure
`:rest:test` failed with `ResultServiceTest.analyzeUsesThePayloadSessionIdAsObjectId`
throwing `java.lang.NullPointerException at ResultServiceTest.java:41`, which is the
field initializer `new HistoryResult(historyDir.toString(), objectMapper)`.

### Root cause
`@TempDir` was declared on an instance field. JUnit Jupiter creates the test instance
first, running every field initializer, and only afterwards injects the temporary
directory into that field. The `historyResult` and `service` field initializers
therefore dereference `historyDir` while it is still `null`.

### Planned modification
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Receive the temporary directory through constructor parameter injection, which JUnit
resolves before field initializers run, and build `historyResult` and `service` from
it inside the constructor. The field is retained so the history assertions can still
read it. No test intent or assertion changes.

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

### Planned modification
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Hold the trace in a `String` and assert against it directly. No assertion change.

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

### Planned modification
`rest/src/test/java/com/unconscious/collective/quiz/ResultServiceTest.java`
Parse the written trace with the existing `objectMapper` and compare the `sessionId`
field, so the assertion is independent of pretty-print layout. The now unused
`assertTrue` import is dropped.
