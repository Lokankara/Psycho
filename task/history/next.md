# Step Impl — Playwright E2E + Python API Tests

## 0. Direct answer

**Yes, Playwright is possible** — for the quiz SPA (`ui`, embedded in the `rest` jar) and the quiz API
(`/api/analysis/*`). Both are live on `https://collective-unconscious.onrender.com`.

**Not possible against production**, and this is the "what needs to move" answer:

| Surface | Status on prod host | Why (verified) |
|---|---|---|
| quiz SPA `/ /quiz /result /history /assessment` | works | `ui/dist` embedded as `static/` — `rest/build.gradle:34-39` |
| `/api/analysis/*` (5 endpoints) | works | `AnalysisController` — `AnalysisController.java:34-124` |
| `/api/agile/*` (4 endpoints) | **404** | `QuizApplication.java:6` is a plain `@SpringBootApplication` in `...quiz`; no `scanBasePackages`/`@Import` anywhere in `rest/src`, so `com.unconscious.collective.agile.controller` is never scanned |
| agile app `:8081` | **not deployed** | `Dockerfile:6` builds `:rest:bootJar` only |
| kanban board `:5174` | **not deployed** | builds as a UMD lib (`kanban/vite.config.ts`), served by nothing |
| `/board` link | **broken in prod** | `ui/src/App.tsx:10,14` → `window.location.replace(VITE_KANBAN_URL ?? 'http://localhost:5174')`; `ui/build.gradle:15-18` never injects `VITE_KANBAN_URL` |

### Verified 2026-10-04 against the live host

| Probe | Result |
|---|---|
| `GET /` | 200, SPA shell (`/assets/index-CEKv6bwT.js`) |
| `GET /api/analysis/questions` | 200, **18 items** — `x1..x6`, `y1..y6`, `z1..z6`, text identical to `rest/src/main/resources/quiz/set_0.json:5-6` |
| `GET /api/analysis/history` | 200, **`[]`** — production DB is empty |
| earlier probes (`/`, `/board`, questions) | **503** — free-tier sleep; host wakes within a minute or so |

**Consequences — the deployed jar is stale.** Production serves only `set_0.json` (18 questions);
the repo now has 42 across `set_0` + `set_1` + `set_2`. Therefore:

- **Never hardcode a question count.** Derive `N` from `GET /api/analysis/questions` in every suite.
  A `== 42` assertion fails on prod; a `== 18` assertion fails against a rebuilt jar.
- `set_1`/`set_2` ids (`x7`–`x10`, `x11`–`x14`) **do not exist on prod** — no test may name them.
- Prod scoring code is an older build than `ResultServiceTest`, so
  `PROPHET_IDEOLOGUE`/`x=y=z=1.0` is **verified for local code only**, not for the deployed build.
  Cannot be probed read-only (`POST /api/analysis` is the only channel and needs a POST).
  → assert *coordinate-sign ↔ octant consistency*; pin the octant name only after a first green run.
- A red test may mean "prod is stale", not "code is broken". Check this before triaging anything.

**To move:** give agile its own service (`./gradlew :agile:bootRun` → Render web service on :8081) and
kanban a static host (or embed `kanban/dist` into a served module). Until then agile/kanban E2E runs
**local-only**.

## 1. Locked decisions

| Decision | Value | Source |
|---|---|---|
| E2E driver | Playwright for Java `1.63.0` | already decided in `task/current/PROGRESS.md:35`, `TODO.md:43` |
| E2E location | `qa/` module, existing `:qa:e2eTest` task | `qa/build.gradle:26-34` |
| Base URL | `https://collective-unconscious.onrender.com`, overridable | `PROGRESS.md:36` |
| API test language | Python + pytest (new) | user request |
| API test location | `qa/api/` | keeps one `qa` root; Gradle ignores `.py` |
| Mutating tests on prod | **allowed** (user accepted) | user decision |
| Scope this iteration | quiz SPA + quiz API on prod; agile/kanban local-only | user decision |

## 2. Blocking prerequisite — leaked credential

`task/current/TODO.md:15` commits a **live Render Postgres password**
(`unconscious_collective_user:zHC5gMLk...@dpg-db1dg0hsrm7s73b6870g-a.oregon-postgres.render.com`).
Prod runs that DB, and these suites write to it. Do this first (`impl_pi.md` Step 9):
replace with a `postgresql://<user>:<password>@<host>/<db>` placeholder, add real value to an
untracked `.env`, add `.env` to `.gitignore`, and rotate the credential on Render.

## 3. Verified API contracts (basis for assertions)

| Endpoint | Returns | Writes? |
|---|---|---|
| `GET /api/analysis/questions` | `List<QuestionResponse>` — 42 locally, **18 on prod** | no |
| `POST /api/analysis` | `AnalysisResponse` | **no — zero side effects** (`AnalysisController.java:84-89`, uses `quizService.evaluate`) |
| `POST /api/analysis/analyze` | `AnalysisResult` | trace file only, **no DB row** (`ResultService.java:72-76`) |
| `POST /api/analysis/results` | `QuizResultResponse` (has `coordinateLabel`, `archetype`) | **DB row + trace file** (`ResultService.java:83-88`) |
| `GET /api/analysis/history` | `List<QuizResult>` newest-first, **unpaginated** | no |
| errors | `ResponseStatusException`/`IllegalArgumentException` → `{"error": "..."}` | `ApiExceptionHandler.java:16-37` |

Deterministic fixture (all-`POSITIVE`, `ResultServiceTest.java:47-61`) — **local code only**, see §0:
`octant=PROPHET_IDEOLOGUE`, `x=y=z=1.0`, `dominant=EVERYMAN`,
`drive=BELONGING_AND_CONNECTION`, `trajectory=SACRIFICE`, `confidence=1.0`, `symbols` non-empty.

**Design rule that minimises prod damage:** assert scoring semantics through `POST /api/analysis`
(zero writes). Reserve `POST /api/analysis/results` for one integration test. Mark every written
row by using a `sessionId` prefixed `e2e-` so rows are identifiable (there is no DELETE endpoint).

## 4. Tasks

### Step 0 — Persist this plan
Copy this file to `task/current/step_impl.md`, log planned modifications in
`task/current/PROGRESS.md` before editing any file (RULES §5), append the commit-message artifact
to `task/current/artifact_pi.md`, move finished steps to `DONE.md`. No `//` comments, no `git`
commands (RULES).

### Step 1 — `qa/build.gradle`
Add per `PROGRESS.md:33-35`: `org.junit.jupiter:junit-jupiter`,
`org.junit.platform:junit-platform-launcher`, `org.springframework.boot:spring-boot-starter`
(QuestionBank needs spring-core at runtime), `com.microsoft.playwright:playwright:1.63.0`.
Add an `installChromium` task running `playwright install chromium` (`mustRunAfter` nothing,
`e2eTest` `dependsOn` it when `PLAYWRIGHT_SKIP_BROWSER_INSTALL` is unset).
`:qa:e2eTest` currently matches **zero** classes — `include '**/*E2ETest.class'` has no match
because `qa/.../qa/TestRunner.java` is an empty class. Step 5 creates the matching class.

### Step 2 — `qa/.../qa/BaseUrl.java` (new)
Resolution order: `-DbaseUrl` → `E2E_BASE_URL` env → default
`https://collective-unconscious.onrender.com`. Plus `warmUp()`:
poll `GET /api/analysis/questions` with bounded backoff until 200.

> Required: the host returned **503 on 3 consecutive probes** — free-tier sleep/cold start. Without
> warm-up every test flakes. Treat 503 as retryable in all HTTP clients.

### Step 3 — `qa/.../qa/Report.java`, `ScreenShot.java`
`Report` writes failure logs to `../logs/e2e/<label>_<stamp>.log`; `ScreenShot` writes Playwright
Chromium PNG to `../logs/e2e/screenshots/`. Both no-op on success. HTML-snapshot fallback if
Chromium is missing.

### Step 4 — Add missing `data-testid` hooks to source
Existing hooks cover quiz/result/assessment only. All visible strings are Russian and
`HistoryPage`/`WelcomePage`/`Layout` have **zero** testids. Add:

| File | Add |
|---|---|
| `ui/src/components/Layout.tsx` | `data-testid="nav-link-{path}"` on each nav link (5 links) |
| `ui/src/pages/WelcomePage.tsx` | `data-testid="question-count"` on the count paragraph (`:28`) |
| `ui/src/pages/HistoryPage.tsx` | `data-testid="history-loading"` (`:34`), `"history-empty"` (`:36`), `"history-table"` (`:40`), `"history-row"` (`:50`) |
| `ui/src/pages/ResultPage.tsx` | already has `dashboard-title`, `coordinate-label`, `octant-badge`, `data-axis` — no change |

### Step 5 — `qa/.../ui/pages/Locators.java` (fix stale selectors)
Verified drift against current markup — three entries are wrong:

| Entry | Status |
|---|---|
| `QUIZ_NEXT_BUTTON = button[data-testid="quiz-next"]` | **phantom — no Next button exists.** `QuizPage.choose()` auto-advances on pole click (`QuizPage.tsx:31-38`). Delete the entry. |
| `DASHBOARD_COORDINATE_LABEL = span[data-testid="coordinate-label"]` | element is a `<p>`, not `<span>` (`ResultPage.tsx:46`) |
| `DASHBOARD_OCTANT_BADGE = span[data-testid="octant-badge"]` | correct tag (`ResultPage.tsx:52`) but **not** nested in `h1` |
| `DASHBOARD_TITLE = h1` | `ResultPage.tsx:45` has `data-testid="dashboard-title"` — prefer the testid |

Add: `NAV_LINK`, `QUESTION_COUNT`, `HISTORY_*`, `QUIZ_PROGRESS` (`QuizPage.tsx:62` text
`Вопрос {n} из {total}`).

### Step 6 — `qa/.../qa/VerifyCoordinateCalculationE2ETest.java` (new)
The class that makes `:qa:e2eTest` non-empty. JUnit 5 `@Test` tagged `@Tag("e2e")` driving
`jbehave-core`'s `Embedder` (`TODO.md:42` — JBehave's bundled runners are JUnit-4 oriented, so do
not use `JUnitStories`). Calls `BaseUrl.warmUp()` in `@BeforeAll`; on assertion failure call
`Report` + `ScreenShot`.

### Step 7 — `qa/src/main/resources/stories/verify_coordinate_calculation.story`
8 scenarios per `PROGRESS.md:45` — 4 existing (determinism, Z-cancellation →
`PROPHET_IDEOLOGUE`, null-pole skip, duplicate-ID last-non-null-wins) + 4 browser/API remote
scenarios:

1. welcome renders the **API-reported** question count (fetch `GET /api/analysis/questions` first,
   assert `question-count` text matches `N`) — never `== 42`
2. nav `/` → `/quiz` → `/history` → `/assessment`
3. full quiz: loop `N` times over `data-pole="POSITIVE"` (N from the API) → `/result` shows
   non-empty `coordinate-label` and `octant-badge`, and `data-axis="X" | "Y" | "Z"` values are all
   equal and positive. **Assert consistency, not `PROPHET_IDEOLOGUE` / `+1.00`** — the deployed
   build may score differently (§0). Once observed green against the deployed build, pin the exact
   octant/coords as a follow-up.
4. deep link `/quiz`, `/result`, `/history`, `/assessment` return the SPA shell (200 + HTML)

### Step 8 — Python API suite `qa/api/` (new)
- `pyproject.toml` + `requirements-dev.txt`: `pytest`, `requests`
- `conftest.py`: session-scoped `base_url` fixture (env `QUIZ_API_BASE_URL`, default prod URL);
  `warm` fixture retrying past 503; register markers `needs_agile`, `needs_kanban`
- `client.py`: `requests.Session` wrapper, `503`/connection retry with backoff

| File | Coverage |
|---|---|
| `test_questions.py` | 200; **no hardcoded count** — assert `len > 0`, ids unique, `axis` ∈ {X,Y,Z}, both poles non-empty, Russian text present. Add `@pytest.mark.drift` comparing against `EXPECTED_QUESTION_COUNT` env only when set |
| `test_scoring.py` | build the answer map **from `GET /questions`**, not a literal id list. all-POSITIVE → coords `x=y=z>0`, all-NEGATIVE → all `<0`, mixed, empty map, unknown id ignored, coords bounded `[-1,1]`, and octant consistent with coordinate signs. **All via `POST /api/analysis`, zero writes** |
| `test_analyze.py` | `objectId == sessionId`; null pole skipped; `symbols` non-empty; `confidence ∈ [0,1]` (no DB write) |
| `test_validation.py` | null pole → 400 `{"error":…}`; unknown pole → 400; both error shapes |
| `test_results_history.py` | `POST /api/analysis/results` → `QuizResultResponse` has `coordinateLabel` + `archetype`; then `GET /api/analysis/history` **contains** the entry. Use `sessionId = "e2e-<uuid>"`. Assert containment, never exact count. |
| `test_spa_contract.py` | `/quiz`, `/result`, `/history`, `/assessment` → 200 HTML; unknown API route → 404 |
| `test_agile_kanban.py` | 4 agile + kanban-board specs, all `@pytest.mark.needs_agile/needs_kanban` → skipped with reason "agile/kanban not deployed on production" |

### Step 9 — Local agile/kanban specs (unblocked, run manually)
`test_agile_kanban.py` targets `AGILE_BASE_URL` (default `http://localhost:8081`) and
`KANBAN_BASE_URL` (default `http://localhost:5174`); deselected unless those env vars are set.
Assert board shape, `PATCH /api/agile/stories/{id}/column`, `POST /api/agile/bdd-execution`.
Kanban card ids are **random per boot** (`JsonAgileBoardService.java:180-182`) → select by visible
`code` (`US-1`, `T-1`), never by `kanban-card-story:<id>`. Note invalid `columnCode` yields **500,
not 400** (`JsonAgileBoardService.java:242`) — assert current behaviour, not desired behaviour.

## 5. Test-design constraints

- `ui/src/api.ts:25,36-48` **de-duplicates concurrent GETs** (StrictMode double-mount) — never assert
  on request counts.
- `ResultPage` reads `sessionStorage['quizResult']` (`ResultPage.tsx:7`) — keep quiz→result in one
  browser context; `/result` shows "Результат недоступен" otherwise.
- `GET /api/analysis/history` is unpaginated and shared with real users — assert containment only.
- No auth exists anywhere, so prod writes are unauthenticated and irreversible.

## 6. Risks

| Risk | Mitigation |
|---|---|
| **Prod jar is stale (18 vs 42 questions)** | Derive `N` from the API everywhere; `@pytest.mark.drift` check; never pin ids or counts. A red test may be deploy drift — check before triaging |
| **Prod scoring differs from local code** | Consistency-based octant/coordinate assertions; pin exact values only after an observed green run |
| Prod rows are permanent, public, unfilterable | Semantics tested via zero-write endpoint; written rows use `e2e-` sessionId marker; cap `POST /results` at one test |
| 503 cold start (observed, 3 probes) | `BaseUrl.warmUp()` + 503 retry in both suites |
| Leaked prod DB credential | Step 2 (prerequisite) |

## 6a. Prod state as of 2026-10-04

`GET /api/analysis/history` → `[]`: **no real users have saved results yet.** Prod writes are
therefore currently low-volume, but the rows are still permanent and public — no `DELETE` endpoint
exists. One-time cleanup after test runs requires direct SQL against the Render Postgres.

## 7. Validation

```
playwright install chromium
./gradlew :qa:e2eTest -DbaseUrl=https://collective-unconscious.onrender.com
python -m pytest qa/api -v
npm --prefix ui run typecheck
```

Green means: `:qa:e2eTest` reports 8 scenarios, `pytest` passes with agile/kanban skipped by marker,
`typecheck` clean, and exactly one new row appears in prod `/api/analysis/history` with an `e2e-`
sessionId. Against the **current** prod build expect `N = 18` questions; after prod is redeployed
with `set_1`/`set_2`, `N` becomes 42 with no test change required.

## 8. Out of scope

CI workflow wiring (`.github/` does not exist in this tree), JaCoCo coverage, history pagination,
`/board` prod fix, deploying agile/kanban, fixing the 3 empty test classes in `agile/src/test`,
and removing the stale `bdd` references in `README.md`.