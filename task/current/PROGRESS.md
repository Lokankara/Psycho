# PROGRESS

Active work only — completed steps live in `DONE.md`; the full implementation plan lives in `impl_pi.md` (Steps 1–10). Log the step's planned modifications here **before** editing any file (RULES §5), then move it to `DONE.md` on completion and append the commit-message artifact to `artifact_pi.md`.

---

## Pending: spec §4 artifact — `task/bug_report_coordinates.md` (`impl_pi.md` Step 1)

- Merge `task/current/bugs/bug_report_coordinates.md` + `task/current/bugs/bug_report_coordinate.md` into `task/bug_report_coordinates.md` (exact spec filename).
- Content: RCA (mutable shared `QuestionBank` + runtime `POST /api/analysis/questions`), JSON execution diffs of the `-1.00 / -0.67 / -0.33` runs, all paths corrected to `/result/history/`.
- Delete the two `task/current/bugs/` sources after the merge.

## Pending: Phase 7 — Validation evidence (`impl_pi.md` Step 2)

- Run `./gradlew build`, `./gradlew :qa:e2eTest`, `npm run build`; route output into `./logs`.
- Append results to `artifact_pi.md`; add Test Results section to `AC.md`.
- Verify identical payload hash ⇒ identical `x/y/z/octant` in `result/history/`.

## Pending: remaining TODO items (`impl_pi.md` Steps 3–10)

- **Step 3** — `GET /api/analysis/history?page=&size=` pagination over `result/history/*.json` + wire `ui/src/pages/HistoryPage.tsx`.
- **Step 4** — `logback-spring.xml` file appender → `./logs` (`quiz.log`, `agile.log`) + controller/service/DAO logging.
- **Step 5** — create `./scripts/analyze_logs.sh` wrapper (RULES §6 path vs `task/tools/analyze_logs.sh`).
- **Step 6** — JaCoCo coverage for `rest` + `agile`; record `ArchetypeScoringService` numbers.
- **Step 7** — kanban board selectors in `qa/.../Locators.java`, `BoardPage` object, `verify_board_interaction.story`.
- **Step 8** — resolve open decisions: browser driver (Playwright vs Selenium), CI `e2eTest` job, logs routing.
- **Step 9** — redact the live PostgreSQL password in `TODO.md` line 15; move to untracked `.env`; rotate credential.
- **Step 10** — doc cleanup: `draft.md` stale/duplicate plan, `review.md` dead refs (`sprint/PI1.md`, `task/history/`), `artifact_pi.md` drift (`bdd`→`qa`, `5.1.0`→`5.2.0`), `task/user_story_acceptance.md:81` + `.claude/skills/logs/SKILL.md:88` path fixes, `AC.md` test evidence.

## Step: impl BDD per plan.md — correct qa module, BASE_URL, failure screenshots/logs

### Planned modifications
- `qa/build.gradle` — add `org.junit.jupiter:junit-jupiter`, `org.junit.platform:junit-platform-launcher`,
  `org.springframework.boot:spring-boot-starter` (QuestionBank needs spring-core at runtime).
  AMENDMENT: Playwright forbidden by user — no browser dependency; ScreenShot captures the BASE_URL
  page as HTML via HttpClient instead of a Chromium PNG.
- `qa/.../qa/BaseUrl.java` (new) — `https://collective-unconscious.onrender.com` default, `-DBASE_URL`/env override.
- `qa/.../qa/Report.java` (new) — failure logs to `../logs/e2e/`.
- `qa/.../qa/ScreenShot.java` (rewrite) — HTTP GET of BASE_URL saved as `../logs/e2e/screenshots/*.html`.
  AMENDMENT 2: JSON request bodies built with `ObjectMapper.writeValueAsString` (user choice) —
  `BddSupport.quote()` / `stringMapJson()` removed, no manual string escaping anywhere.
  AMENDMENT 5 (user): UI layer uses **Selenide** (`import static com.codeborne.selenide.*`) plus
  `io.github.bonigarcia:webdrivermanager:6.4.0` for chromedriver resolution; `selenium-java` is no
  longer declared (Selenide pulls Selenium transitively). `Browser.java` configures Selenide
  (chrome, headless, 1440x1080, 30s timeout, screenshots + page source), `ScreenShot` uses
  `Selenide.screenshot`, page objects return Selenide selector strings again.
- `qa/.../ui/pages/QuizViewPage.java` (new) — spec §1 page object.
- `qa/.../ui/steps/CoordinateCalculationSteps.java` — keep 4 scenarios' steps, add trace recording to
  `../result/history`, remote (BASE_URL) steps, archetype-matching steps, bounded-coordinates step,
  `verify()` wrapper (screenshot+log on assertion failure).
- `qa/.../ui/steps/HistoryStep.java` — rewrite: remove duplicate steps, keep unique remote-persistence steps.
- `qa/.../qa/VerifyCoordinateCalculationE2ETest.java` (new) — JUnit5 + JBehave Embedder runner, evidence capture on failure.
- `qa/src/main/resources/stories/verify_coordinate_calculation.story` — 8 scenarios (4 existing + 4 new).
- `task/bdd_specifications.md` — fix paths/packages/fields, catalog new steps + runner + screenshots.
- `task/current/TODO.md` — resolve browser-driver decision (Playwright 1.63.0 + `playwright install chromium`).
- `task/current/artifact_pi.md` — commit artifact; step moved to `DONE.md` on completion.

### Expected outcome
`./gradlew :qa:e2eTest` runs 8 scenarios against local scoring + BASE_URL; any failed
step writes `../logs/e2e/<label>_<stamp>.log` + `../logs/e2e/screenshots/<label>_<stamp>.png`.


