# TODO
add  update dashboard like index.html
add log for controllers and services and dao layers, add logback.xml appender - AC must save to file
add locators to UI and kanban for bdd steps
create agile with spring and PI
add kanban submodule with ui
check coverage test
add test and check kanban dashboard with bdd and rest-api test

save history result in json files, add load files from result/history with pagination
investigate them and find error or bugs in logic
add e2e test to verify logic - use Bdd jbehave
create user story - acceptance criteria
save all in /task/*.md as like artifact i for next iteration
save in DB, add env file for postgresql://unconscious_collective_user:zHC5gMLkNahR1vZIwDcKBKwCwRW0SyuW@dpg-db1dg0hsrm7s73b6870g-a.oregon-postgres.render.com/unconscious_collective
parse log and find error warn
add like trello and interact with kanban

Fix the scoring logic so it is single-path and deterministic.
Log execution data to JSON for bug analysis.
Add BDD acceptance tests using page object locators.
Generate the required task artifacts in /task/*.md.
Extend the project with Agile persistence and a board UI.
Locators & BDD Steps: JBehave steps with Page Object Model CSS/XPath locators.

JSON Execution History: Logging run outputs to JSON files to investigate calculation drift (like the -1.00 vs -0.67
error).

E2E Tests: JBehave story files verifying coordinate determinism.

Artifacts: Markdown files generated in /task/*.md (user_story_acceptance.md, bug_report_coordinates.md,
bdd_specifications.md).

Agile/SAFe Entities & DB Persistence: Spring Data JPA models for Program Increments (PI), Epics, Stories, and Sprints.

Kanban Interface: Interactive Trello-style board controller linked to Agile entity states.



Uncertain or missing requirements registered per `task/RULES.md` §2. Matching `//TODO` markers are placed in the referenced code.

- **JBehave + JUnit Platform.** `rest/build.gradle` uses `useJUnitPlatform()`, and JBehave's bundled runners (`JUnitStories`) are JUnit-4 oriented. The exact artifact/version coordinates and whether an official JUnit 5 runner exists could not be verified because outbound web requests were declined. The planned approach is to drive `jbehave-core`'s `Embedder` from a plain JUnit 5 `@Test` tagged `@Tag("e2e")` in a separate Gradle `e2eTest` task. Confirm the preferred JBehave version before implementation.
- **Browser driver for Page Object Models.** `task/current/BDD.md` §1 requires POM with XPath/CSS locators but does not name the driver. Java (Chromium) is the proposed choice; Selenium + WebDriverManager is the alternative. Confirm before adding the dependency.
- **Artifact locations.** `task/RULES.md` names `PROGRESS.md`, `TODO.md` and `artifact_pi.md` without a directory. They are currently created at the repository root; move them under `task/` if iteration artifacts should stay grouped.
- **`./logs` routing.** `task/RULES.md` §6 requires all command output and runtime logs in `./logs`. Confirm whether build output should be redirected there manually or wired automatically.
- **CI.** Whether `.github/workflows/ci.yml` should also run `./gradlew e2eTest` (browser + network required) is unspecified.
