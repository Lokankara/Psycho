### Prompt: User Story to BDD Test Generation via Dark Factory Pattern (JSON Output)

### TASK OVERVIEW

Implement an automated "Dark Factory" execution pipeline that consumes User Stories and Trello/Kanban board states
formatted as JSON data, parses acceptance criteria, and generates/executes BDD test scenarios without manual coding
intervention.

---

### 1. JSON Schema Specification (Trello / Kanban Board Compatible)

All User Stories and Kanban board items must be stored and processed in `api/user-story/` using the following JSON
structure (reflecting board columns: "Bug", "User Story", "Review", "Todo", "Done" ):

example
```json
{
  "board": {
    "columns": [
      {
        "id": "col_templates",
        "title": "Шаблоны",
        "cardsCount": 5
      },
      {
        "id": "col_in_review",
        "title": "На проверке",
        "cardsCount": 14
      },
      {
        "id": "col_ready_us",
        "title": "Готовые User Story",
        "cardsCount": 38
      },
      {
        "id": "col_tc_review",
        "title": "TC_review [Выбрали US>Пишем ТС > ждем Проверку]",
        "cardsCount": 0
      }
    ]
  },
  "userStory": {
    "id": "US_001.001",
    "title": "US_001.001 | Main Page > Main Page > Viewing the main page",
    "status": "col_in_review",
    "label": {
      "color": "green",
      "text": "User Story"
    },
    "feature": "Assessment API Processing",
    "asA": "client application",
    "iWantTo": "submit quiz answers via the /api/analysis endpoint",
    "soThat": "I can obtain a deterministic calculation of coordinates, matched octant, and narrative archetype",
    "acceptanceCriteria": [
      {
        "scenarioId": "SC_001",
        "scenarioTitle": "Deterministic Assessment Score Calculation",
        "given": [
          "a valid AssessmentPayload containing quiz answers"
        ],
        "when": [
          "a POST request is submitted to /api/analysis"
        ],
        "then": [
          "the response status should be 200 OK",
          "the resulting coordinates X, Y, Z must be deterministically rounded to 2 decimal places",
          "the response includes matched octant and coreDrive values"
        ]
      }
    ]
  }
}

```

---

### 2. Dark Factory BDD Engine Integration

* **Engine Purpose:** Read `.json` user story definitions from `api/user-story/` at runtime or build step, parse
  Given-When-Then blocks, and synthesize execution steps automatically.
* **Implementation Strategy:**
* Build/configure a dynamic BDD generator (Dark Factory parser) in `rest/src/test/java/.../bdd/factory/`.
* Automatically map JSON tokens from acceptance criteria directly to API request templates (`/api/**`) without requiring
  manually written step definitions for every story.
* Utilize centralized element/endpoint locators from `Locators.java`.

---

### 3. Execution & Workflow Constraints

#### Pre-Execution Protocol:

* **BEFORE EDITING ANY FILE:** Record the planned steps in `PROGRESS.md`.

#### Engineering & Code Rules:

* Apply SOLID, DRY, KISS, and YAGNI principles.
* **NO INLINE COMMENTS:** Do NOT use `//` anywhere in the code.
* **NO HALLUCINATIONS:** If an endpoint contract or mapping is missing or unclear, insert `//TODO` in code and append
  the exact task to `TODO.md`.
* **EXECUTION RESTRICTIONS:** DO NOT execute `gradlew` or `git` commands.

#### Logging & Analytics:

* Save test runner output, logs, and stack traces into `./logs/dark_factory.log` and `./logs/dark_factory.err`.
* Analyze logs using:
  `./scripts/analyze_logs.sh --path=./logs`

#### Artifact Generation & Commit Messaging:

* Formulate a Conventional Commit message for the completed step (as text output).
* Append step completion details, generated BDD test status, and commit message into `DONE.md`.
