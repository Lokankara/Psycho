### Task Overview
Execute an end-to-end bug investigation, implement BDD testing using JBehave, persist execution artifacts, and build an Agile/SAFe management domain model with DB persistence and a Trello-like UI/API interface.

---

### 1. BDD Steps & Element Locators
- **Locators Setup:**
    - Define Page Object Models (POM) and UI element locators (XPath / CSS selectors) for all UI components (Quiz View, Dashboard, Axis Controls).
- **JBehave BDD Integration:**
    - Implement step definitions using JBehave (`@Given`, `@When`, `@Then`).
    - Add BDD steps to cover full quiz completion, coordinate generation, and archetype matching.

---

### 2. History Logging & Bug Investigation
- **JSON Serialization:**
    - Save every test execution result and coordinate calculation state into structured JSON files under `/result/history/` (e.g., `result_YYYYMMDD_HHMMSS.json`).
- **Logic Investigation:**
    - Analyze differences in Z-axis calculations (`-1.00` vs `-0.67`) across historical runs.
    - Locate floating-point rounding issues, missing axis answers, or state leakage in `ScoringService`.

---

### 3. E2E Logic Verification Test
- **JBehave Story File:**
    - Create `verify_coordinate_calculation.story` to test calculation determinism across identical input payloads.
- **Assertion:**
    - Verify that identical answer sets consistently yield deterministic coordinates $X, Y, Z$ and identical `Octant` mapping without drift.

---

### 4. Artifact Generation (`/task/*.md`)
Generate markdown artifacts in the `/task/` folder for the next development iteration:
- `/task/user_story_acceptance.md`: Acceptance criteria in Given-When-Then format.
- `/task/bug_report_coordinates.md`: Root cause analysis of coordinate fluctuation with JSON execution diffs.
- `/task/bdd_specifications.md`: Full catalog of BDD step mappings and page locators.

---

### 5. Agile, SAFe & Spring DB Domain
- **Data Model & Entities:**
    - Create JPA/Spring Data entities: `ProgramIncrement` (PI), `Epic`, `UserStory`, `Task`, `Sprint`, `KanbanColumn`.
- **Database Persistence:**
    - Configure PostgreSQL/H2 persistence layer for tracking Agile artifacts across Program Increments (PIs).

---

### 6. Trello-like Kanban Interface (Armain/Board)
- **Board Controller & UI:**
    - Implement a Kanban board UI/API (Spring Boot + Vaadin or REST) allowing users to drag and drop stories/tasks across workflow columns (`To Do`, `In Progress`, `Code Review`, `Done`).
- **Agile Integration:**
    - Link BDD test execution statuses directly to User Stories and Tasks on the board.
 