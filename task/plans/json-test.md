### Prompt: JSON Storage Service, Proxy Controller & RestAssured API Tests

#### TASK OVERVIEW

Implement a file-based JSON storage service with a Spring `JsonProxyController` in the `agile` module and write comprehensive RestAssured integration tests to verify all CRUD operations.

---

### 1. File Storage Service & Controller Implementation

#### Storage Path Specification:

* Base Directory: `agile/src/main/resources/json/{category}/{filename}.json`
* Categories supported: `bugs`, `task`, `story`.
* Configuration: Define directory base path in `agile/src/main/resources/application.properties`.

#### API Contracts (`JsonProxyController`):

* `GET /api/json/bugs` — List all JSON files/payloads stored under `.../json/bugs/`.
* `GET /api/json/task` — List all JSON files/payloads stored under `.../json/task/`.
* `GET /api/json/story` — List all JSON files/payloads stored under `.../json/story/`.
* `GET /api/json/{id}` — Retrieve the raw content of the specified JSON file by ID/path.
* `PUT /api/json/{id}` — Write/persist the request body into `{id}.json`. Return `201 Created` if a new file was written, or `200 OK` if overwritten.
* `DELETE /api/json/{id}` — Delete the specified JSON file and return `{"status":"deleted"}` with HTTP 200.

---

### 2. RestAssured API Integration Tests

Create `JsonProxyControllerTest.java` in `agile/src/test/java/.../controller/` using RestAssured.

#### Test Scenarios to Cover:

1. **CREATE (`PUT /api/json/task/TSK_001`):**
* Submit a valid JSON payload.
* Assert HTTP Status `201 Created` on initial creation.
* Assert file is actually created in `agile/src/main/resources/json/task/TSK_001.json`.


2. **OVERWRITE (`PUT /api/json/task/TSK_001`):**
* Submit updated JSON payload for the same resource ID.
* Assert HTTP Status `200 OK`.


3. **READ ALL BY CATEGORY (`GET /api/json/task`):**
* Execute request to list tasks.
* Assert HTTP Status `200 OK` and response contains the created JSON payload.


4. **READ BY ID (`GET /api/json/task/TSK_001`):**
* Execute request for specific file content.
* Assert HTTP Status `200 OK` and JSON fields match the saved state.


5. **DELETE (`DELETE /api/json/task/TSK_001`):**
* Issue delete request.
* Assert HTTP Status `200 OK` and response body equals `{"status":"deleted"}`.
* Assert subsequent `GET /api/json/task/TSK_001` returns `404 Not Found`.

---

### 3. Constraints & Workflow Rules

#### Pre-Execution Logging:

* **BEFORE EDITING ANY FILE:** Record planned actions in `PROGRESS.md`.

#### Engineering Constraints:

* Adhere strictly to SOLID, DRY, KISS, and YAGNI.
* **NO INLINE COMMENTS:** Do NOT use `//` anywhere in the Java code (except `//TODO` for missing specifications).
* **NO HALLUCINATIONS:** If file paths, permissions, or properties are missing, insert `//TODO` in code and append to `TODO.md`.
* **EXECUTION RESTRICTIONS:** DO NOT run `gradlew` or `git` commands.

#### Logging & Artifacts:

* Route test and controller outputs to `./logs/json_storage.log` and `./logs/json_storage.err`.
