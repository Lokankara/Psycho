# impl_pi.md — How to Implement All (Iteration Master Document)

**Created:** 2026-10-05
**Supersedes:** the conflicting plan blocks in `draft.md` (Phase 0–7 *and* P1–P7) and the stale checklist in `review.md`.
**Authoritative spec:** `task/current/BDD.md` §1–§6. **Constraints:** `task/RULES.md`.
**Status of completed steps:** see `task/current/DONE.md` (moved from `PROGRESS.md` per iteration convention).

---

## 1. Verified Status Matrix (spec § → repository evidence)

| § | Requirement | Status | Evidence (verified path) |
|---|-------------|--------|--------------------------|
| 1 | POM locators + JBehave step definitions | ✅ Done | `qa/src/main/java/com/unconscious/collective/qa/ui/pages/Locators.java`, `AxisControlsPage.java`, `DashboardPage.java`, `ui/steps/CoordinateCalculationSteps.java`, `ui/steps/HistoryStep.java` |
| 2 | JSON execution history in `/result/history/` | ✅ Done | `HistoryResult` (`rest/.../domain/history/HistoryResult.java`, default dir `../result/history`), 8 files in `result/history/result_*.json`, wired into `ResultService` |
| 2 | Z-axis bug investigation documented | ⚠️ Partial | RCA exists at `task/current/bugs/bug_report_coordinates.md` — **not at the spec-mandated path** `task/bug_report_coordinates.md` |
| 3 | `verify_coordinate_calculation.story` determinism | ✅ Done | `qa/src/main/resources/stories/verify_coordinate_calculation.story`, `e2eTest` task in `qa/build.gradle` (`jbehave-core:5.2.0`) |
| 4 | Three artifacts in `/task/*.md` | ⚠️ 2/3 | ✅ `task/user_story_acceptance.md`, ✅ `task/bdd_specifications.md`, ❌ `task/bug_report_coordinates.md` missing |
| 5 | JPA entities: PI, Epic, UserStory, Task, Sprint, KanbanColumn | ✅ Done | `agile/.../model/entity/*.java`, repos in `dao/`, `AgileController`, H2/Postgres/JSON configs |
| 6 | Trello-like board with BDD status linkage | ✅ Done | `kanban/src/BoardPage.tsx`, `BoardColumn.tsx`, `KanbanCard.tsx`, `DetailDrawer.tsx`, `BddStatusBadge.tsx`; `ui/src/App.tsx` routes `/board` → `KANBAN_URL` (:5174); `AgileBoardService.recordExecution` + `BddExecutionRequest/Response` |
| — | Scoring determinism (prerequisite) | ✅ Done | `ArchetypeScoringService.score(List, AssessmentPayload)`, `AssessmentPayload.answersFor` (null-skip + dedup), `QuestionBank` immutable (`List.copyOf` + `requireUniqueIds`), `ArchetypeScoringServiceDeterminismTest` |

---

## 2. Authoritative Decisions (resolve all doc conflicts)

1. **Artifact filenames win verbatim** (spec §4): `task/user_story_acceptance.md`, `task/bug_report_coordinates.md`, `task/bdd_specifications.md`. The alternatives in `draft.md` (`bug-coordinates-instability.md`, `iteration-02-backlog.md`) are **rejected**.
2. **History path is `/result/history/`** (spec §2 — matches the code default `../result/history`). `task/history/` mentions in `draft.md`, `review.md`, `task/user_story_acceptance.md:81` and `.claude/skills/logs/SKILL.md:88` are **wrong** and get corrected in Step 10.
3. **Module map** (older docs say `bdd`): BDD/E2E module = **`qa`**; SAFe domain = **`agile`**; board UI = **`kanban`**; scoring/history = **`rest`**; quiz UI = **`ui`**. `settings.gradle`: `rest`, `ui`, `qa`, `kanban`, `agile`.
4. **JBehave:** `org.jbehave:jbehave-core:5.2.0` (not 5.1.0 as in `artifact_pi.md`), driven by `Embedder` from a plain JUnit 5 `@Test` in the `e2eTest` task — no `junit-vintage-engine`.
5. **Name alias:** spec's `ScoringService` = code's `ArchetypeScoringService`.
6. **Browser driver default:** Playwright-for-Java (self-managing Chromium); Selenium + WebDriverManager is the fallback. *Open decision — confirm before adding the dependency (Step 8).*
7. **No `//` comments** (except `//TODO`), **no `git` execution** — `task/RULES.md` §2–§3.

## 3. Remaining Implementation Steps (ordered)

### Step 1 — Create the missing artifact `task/bug_report_coordinates.md` (spec §4)
- Merge `task/current/bugs/bug_report_coordinates.md` + duplicate `task/current/bugs/bug_report_coordinate.md` (singular) into `task/bug_report_coordinates.md`.
- Content: RCA (root cause = mutable shared `QuestionBank` + runtime `POST /api/analysis/questions` mutating the question set; the arithmetic itself is pure), JSON execution diffs of the three drift runs (`-1.00 / -0.67 / -0.33`), all path references corrected to `/result/history/`.
- Remove the two `task/current/bugs/` files after the merge (single source of truth).

### Step 2 — Record verification evidence (RULES §5/§6; closes `review.md` §4)
- Run and capture output into `./logs`:
  ```
  ./gradlew build
  ./gradlew :qa:e2eTest
  npm run build   (ui module; kanban has its own vite build)
  ```
- Append results + coverage numbers to `artifact_pi.md`; add a "Test Results" section to `AC.md` (its current gap per `review.md` §1).
- Confirm `result/history/` contains identical-payload-hash pairs with identical `x/y/z/octant`.

### Step 3 — History pagination endpoint (TODO.md)
- `GET /api/analysis/history?page=&size=` listing `result/history/result_*.json` (newest first), returning `{items, page, size, total}`; page item = parsed `ExecutionTrace`.
- Files: `rest/.../controller/AnalysisController.java`, new `rest/.../service/HistoryQueryService.java`.
- Wire `ui/src/pages/HistoryPage.tsx` to consume it (page exists but no paginated API yet).

### Step 4 — Logging to `./logs` (TODO.md item 1; RULES §6)
- `rest/src/main/resources/logback-spring.xml`: rolling file appender → `./logs/quiz.log` (+ `.err` for the ERROR threshold); keep console appender.
- Add INFO/DEBUG logging to controllers, services and DAO layers via SLF4J (`LoggerFactory`) — no `//` comments.
- Same appender for the `agile` module (log file `./logs/agile.log`).

### Step 5 — Log analysis script path (RULES §6 conflict)
- RULES mandates `./scripts/analyze_logs.sh --path=./logs`; actual script is `task/tools/analyze_logs.sh`.
- Create `./scripts/analyze_logs.sh` delegating to `task/tools/analyze_logs.sh` (or move + wrapper), then run it and store output under `./logs`.

### Step 6 — Test coverage check (TODO.md item 5)
- Apply JaCoCo (`jacoco` plugin) to `rest` and `agile`; generate the report and record `ArchetypeScoringService` coverage in `artifact_pi.md`.
- Add gap tests if coverage is below the agreed threshold.

### Step 7 — Kanban/board locators + BDD steps (TODO.md items 2, 6)
- Extend `qa/.../ui/pages/Locators.java` with board selectors (columns `To Do / In Progress / Code Review / Done`, `KanbanCard`, `DetailDrawer`, `BddStatusBadge`).
- New page object `BoardPage` + steps: Given a seeded PI with stories / When a story is moved / Then column and `executionStatus` update — asserting through the `POST /api/agile/bdd/execution` linkage (`AgileBoardService.recordExecution`).
- Story file: `qa/src/main/resources/stories/verify_board_interaction.story`, included in `e2eTest`.

### Step 8 — Resolve open decisions (TODO.md "uncertain" section)
- **Browser driver:** Playwright vs Selenium → record the answer in `TODO.md`, then add the dependency to `qa/build.gradle`.
- **CI:** whether `.github/workflows/ci.yml` runs `:qa:e2eTest` (needs browser + network) — default: separate, non-blocking job.
- **Artifact locations:** resolved by this iteration — iteration files live in `task/current/`; spec deliverables in `task/`.
- **Logs routing:** default is manual tee into `./logs` per `.claude/skills/logs/SKILL.md`.

### Step 9 — Security: redact live credential (TODO.md item 15)
- `task/current/TODO.md` line 15 contains a live PostgreSQL connection string with password for `dpg-db1dg0hsrm7s73b6870g-a.oregon-postgres.render.com`.
- Replace with a `postgresql://<user>:<password>@<host>/<db>` placeholder; move the real value to an untracked `.env` (add `.env` to `.gitignore`); recommend rotating the credential on Render.

### Step 10 — Documentation cleanup (closes `review.md` §2)
- `draft.md`: delete the stale "What I verified (nothing exists yet)" section and the duplicate "__Plan (7 phases)__" P1–P7 block — this file replaces them.
- `review.md`: fix dead references — `sprint/PI1.md` does not exist; checklist item 2 must read `result/history/result_*.json`.
- `artifact_pi.md`: correct `bdd/build.gradle` → `qa/build.gradle`, JBehave `5.1.0` → `5.2.0`, `./scripts/analyze_logs.sh` path (Step 5).
- `task/user_story_acceptance.md:81` and `.claude/skills/logs/SKILL.md:88`: `task/history/` → `/result/history/`.
- `AC.md`: add the test-results section (Step 2).

---

## 4. Validation

```
./gradlew build          # unit + JPA tests, full multi-module
./gradlew :qa:e2eTest    # JBehave stories + history JSON output
npm run build            # ui (vite)
curl http://localhost:8080/api/agile/board   # HTTP smoke (agile module)
./scripts/analyze_logs.sh --path=./logs
```

Acceptance: all commands green, `result/history/` grows by one JSON per run, identical payload hash ⇒ identical `x/y/z/octant`, three artifacts present at exact spec paths, no `//` comments, no `git` commands executed.

---

## 5. RULES.md Compliance Checklist

- [ ] `PROGRESS.md` updated **before** every file edit (pre-edit step log)
- [ ] Completed steps moved from `PROGRESS.md` → `DONE.md`
- [ ] Conventional commit message artifact appended to `artifact_pi.md` after each step
- [ ] No `//` comments (except `//TODO` → mirrored in `TODO.md`)
- [ ] No `git` commands executed
- [ ] All runtime/command output routed to `./logs`


