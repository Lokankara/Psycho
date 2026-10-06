### Prompt: History Results File Storage, Pagination & Delete API (RestAssured & Front-End Integration)

#### TASK OVERVIEW
Implement file management for history result logs stored in 
`C:\dev\projects\AI\Psycho\rest\src\main\resources\history`. Expose paginated read endpoints, 
single-item delete operations, and integrate corresponding UI controls 
(Delete button and Pagination controls) supported by RestAssured verification tests.

---

### 1. Specification & Requirements

#### File Storage Path & System Properties:
- Directory: `C:\dev\projects\AI\Psycho\rest\src\main\resources\history`
- Property Key: `rest.history.path` configured in `rest/src/main/resources/application.properties`.

#### REST API Contracts (`AnalysisController`):
- `GET /api/analysis/history?page={page}&size={size}`:
    