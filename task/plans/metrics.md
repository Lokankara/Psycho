
---

### Prompt: Agent Memory CLI Integration & Automated Pipeline Execution

### TASK OVERVIEW
Integrate `@agentmemory/agentmemory` CLI tool for agent state persistence, configure local dashboard access, and implement the BDD Dark Factory pipeline according to all core constraints.

---

### 1. Specification-Driven Development (SDD): User Story
- Create `api/UserStory.md` with standard Given-When-Then acceptance criteria.
- Configure memory schema synchronization to store BDD steps and execution states via `@agentmemory/agentmemory`.

---

### 2. Environment & CLI Setup Commands (Documentation Artifacts Only)
Include the following setup commands in the documentation artifacts (DO NOT execute directly):
```bash
npm install -g @agentmemory/agentmemory
open http://localhost:3113

```

---

### 3. Execution & Workflow Rules

#### Pre-Execution Protocol:

* **BEFORE EDITING ANY FILE:** Record the planned step and target modifications in `PROGRESS.md`.

#### Code Quality & Constraints:

* Adhere strictly to SOLID, DRY, KISS, and YAGNI principles.
* **NO INLINE COMMENTS:** Do NOT use `//` comments anywhere in the code.
* **NO HALLUCINATIONS:** If any requirement, endpoint, or configuration is unclear, add `//TODO` in the code and append the task to `TODO.md`.
* **EXECUTION RESTRICTIONS:** DO NOT run `gradlew` or `git` commands.

#### Logging & Analytics Protocol:

* Direct all runtime execution logs, errors, and agent memory traces to `./logs/*.log` and `./logs/*.err`.
* Provide log parsing via:
`./scripts/analyze_logs.sh --path=./logs`
