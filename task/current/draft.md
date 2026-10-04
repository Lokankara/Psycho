__Missing (spec §4 deliverables):__

- `task/user_story_acceptance.md` ❌
- `task/bug_report_coordinates.md` ❌
- `task/bdd_specifications.md` ❌

__Also missing:__ `task/history/` (§2 JSON output — directory doesn't exist), and all of the code for Phases 0, 1, 3, 5,
6.

I notice `task/init/` now holds the old artifacts while `current/` holds this iteration's spec — I'll follow that same
convention: new iteration artifacts go in `task/` top-level as the spec dictates (`/task/*.md`), with generated JSON
under `task/history/`.

## Spec → code mapping (2 name mismatches noted)

- Spec says __`ScoringService`__ → actual class is `ArchetypeScoringService` (+ `ResultService`).
- Spec says __"Spring Boot + Vaadin or REST"__ → Vaadin was already removed, so __REST + React__.
- Spec entity list is exact: `ProgramIncrement`, `Epic`, `UserStory`, `Task`, `Sprint`, `KanbanColumn`.
- Artifacts have exact filenames — I'll use them verbatim.
- Constraint from `task/pr.md:21`: __no `//` comments__ anywhere.

---

## Phase 0 — Fix the bug (prerequisite; §2 & §3 depend on it)

The determinism story can't pass until scoring is deterministic.

- `ArchetypeScoringService.score(List<BipolarQuestion>, AssessmentPayload)` — single calculation path, all accumulators
  __local__ inside `score()`, no fields.
- Loop `Axis.values()`, call `payload.answersFor(axis, questionAxisMap)`; fix `answersFor` to _
  _skip `chosenPole == null`__ (today it NPEs) and to dedup by question id (last non-null wins) so duplicate answers
  can't inflate `N`.
- `Score = Σ Pole.sign() / N`, `0.0` when `N = 0`, then `Math.round(v * 100.0) / 100.0`.
- Delete `ResultService.score()`'s manual `HashMap` loop.
- Keep `POST /api/analysis` alive by having `QuizService.evaluate` convert its map → `AssessmentPayload` (no public
  endpoint removed; one calc path only).
- Update `ArchetypeScoringServiceTest` for the new signature.

## Phase 1 — JSON execution history (§2)

- `HistoryLogger` writing `task/history/result_YYYYMMDD_HHMMSS.json` per run.
- Payload: answers, per-axis `{sum, count}`, coordinates __before and after__ rounding, octant, payload hash, timestamp.
- Acceptance: identical payload hash ⇒ identical `x/y/z/octant`.

## Phase 2 — Bug investigation (§2)

- Diff the `-1.00 / -1.00 / -1.00` vs `-1.00 / -1.00 / -0.67` records from `task/history/`.
- Test three hypotheses the spec names: __floating-point rounding__, __missing axis answers__, __state leakage__.
- Output → __`task/bug_report_coordinates.md`__ (root cause + JSON execution diffs).

## Phase 3 — JBehave + POM locators (§1, §3)

- `rest/build.gradle`: `org.jbehave:jbehave-core` + browser driver; new `e2eTest` Gradle task so `./gradlew build` stays
  green without a browser.
- __POM__ in `rest/src/test/java/.../bdd/pages/`: `QuizViewPage`, `DashboardPage`, `AxisControlsPage` — all CSS/XPath
  selectors held in __one `Locators` holder__, never inline in steps.
- Step defs (`@Given/@When/@@Then`) covering: full quiz completion, coordinate generation, archetype matching.
- Story `verify_coordinate_calculation.story`: identical answer sets ⇒ deterministic `X, Y, Z` + identical `Octant`, no
  drift.
- JBehave runs via its `Embedder` from a plain JUnit 5 `@Test` (build uses `useJUnitPlatform()`; JBehave's bundled
  runners are JUnit-4 oriented).

## Phase 4 — Artifacts (§4), exact paths

- `/task/user_story_acceptance.md` — Given/When/Then AC
- `/task/bug_report_coordinates.md` — RCA + JSON diffs
- `/task/bdd_specifications.md` — step↔page↔locator catalog

## Phase 5 — Agile/SAFe domain in DB (§5)

- JPA: `ProgramIncrement`, `Epic`, `UserStory`, `Task`, `Sprint`, `KanbanColumn`; FKs PI → Epic → Story → Task, Sprint →
  Story, column on Story/Task.
- Spring Data repos + `/api/agile/**` REST; H2 default, existing `postgres` profile unchanged; seed PI1 with 3 sprints,
  4 columns, sample stories.

## Phase 6 — Trello-like board (§6)

- React `/board`: columns `To Do`, `In Progress`, `Code Review`, `Done`, drag-and-drop via `@dnd-kit` (
  keyboard-accessible), PI/Sprint switcher, story/task detail drawer with ACs.
- __BDD linkage__: each `UserStory`/`Task` carries `bddStory`, `executionStatus`, `lastRunAt`; after a story run the e2e
  posts results to `/api/agile/**` so pass/fail colors the cards.

## Phase 7 — Validation

`./gradlew build` (unit + JPA), `./gradlew e2eTest` (stories + history JSON), `npm run build`, HTTP smoke of
`/api/agile/board`.

__Flagged risk:__ browser binaries for the POM layer need `playwright install chromium` at first run —
network-dependent, which is why Phase 3 sits in its own `e2eTest` task rather than the default `test` task.

## What I verified (nothing exists yet)

- __No__ JBehave/stories/steps, __no__ agile/sprint/epic/board code, __no__ Playwright/Selenium/RestAssured, __no__ JSON
  history files — all greenfield.
- `task/` already holds `desc.md, impl.md, plan.md, pr.md, ui.md, SYSTEM.md, todo.md, project_architecture_plan.md`.
  Note `task/pr.md:21` → __"DONT USE COMMENTS //"__.
- Baseline scoring code is __unchanged__ from before: `ArchetypeScoringService.score(List, Map)` (no 2-decimal
  rounding), `ResultService.score()` still has the manual `HashMap` extraction loop, and
  `AssessmentPayload.answersFor(...)` still exists but is __dead__. That refactor from the last turn was never applied —
  the scope question was never answered.

## Plan (7 phases)

__P1 — History → JSON files__ Add JSON persistence of quiz results: on each `POST /api/analysis/results`, also write
`data/history/<sessionId>.json`, plus a one-off export of existing H2 rows. Producer: small `HistoryJsonExporter` behind
`ResultService` (write-behind, never fails the request).

__P2 — Investigate the JSONs → find the logic bug__ Fingerprint each record (answer multiset + per-axis `N` + raw sum vs
stored coordinate) to explain the `-1.00 … -1.00` vs `-1.00 … -1.00, -0.67` pair you reported. Candidate defects to
confirm or eliminate: missing 2-dedup in `answersFor`, `answersFor` NPE on `chosenPole == null`, two divergent scoring
paths (`ResultService` vs `QuizService.evaluate`), unrounded coords feeding `Octant.from`. Output →
`task/bug-coordinates-instability.md`.

__P3 — JBehave BDD + E2E__ Gradle deps (`org.jbehave:jbehave-core`) in `rest/build.gradle`,
`rest/src/test/resources/stories/*.stories`, step classes under `rest/src/test/java/.../steps/`, and __locators
extracted into a dedicated `Locators` holder__ (no inline selectors in step methods).

__P4 — User story + acceptance criteria → `task/*.md` artifacts__ Iteration-ready docs: `task/iteration-02-backlog.md` (
user story + Given/When/Then AC, traceable 1:1 to the `.stories`), `task/pi-01-plan.md`, `task/history-export/*.json`.

__P5 — Agile in DB__ JPA: `ProgramIncrement` → `Sprint` → `Story` → `AcceptanceCriterion` (+ enum board column). Repos +
CRUD/move REST under `/api/agile/**`. H2 default, reuses the existing `postgres` profile. Seed a PI with 3 sprints.

__P6 — Trello-like board UI (`/board`)__ React Kanban: columns per status, cards with story points/priority,
drag-and-drop to change column, PI/Sprint switcher, AC drawer. Talks to `/api/agile/**`.

__P7 — Validate__ `./gradlew build` (JUnit Platform + stories), `npm run build`, HTTP smoke of `/api/agile/**`.

__Risks I'd plan around:__ JBehave's native runners are JUnit-4-oriented while this build uses `useJUnitPlatform()` — so
I'd drive JBehave through its `Embedder` from a plain JUnit 5 `@Test` rather than pulling in `junit-vintage-engine` (
unverifiable externally: your



