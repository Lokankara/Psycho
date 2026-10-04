# Review — `task/current/` (Iteration Spec Set)

**Reviewed:** 2026-10-04
**Scope:** `AC.md`, `BDD.md`, `draft.md`, `TODO.md`, `sprint/PI1.md`, against `task/RULES.md`

---

## 1. File-by-File Assessment

### `BDD.md` — Source specification (6 sections)
| § | Requirement | Status per `AC.md` |
|---|---|---|
| 1 | POM locators + JBehave steps | ✅ `Locators.java`, Page Objects |
| 2 | JSON history + Z-axis investigation | ✅ `HistoryResult` (investigation partial — see §3) |
| 3 | `verify_coordinate_calculation.story` determinism | ✅ story + `e2eTest` runner |
| 4 | `/task/*.md` artifacts (3 exact filenames) | ⚠️ claimed in `AC.md`, **unverified** |
| 5 | JPA entities (exact list of 6) | ✅ implemented |
| 6 | Trello-like board (BDD status linkage) | ✅ `/board` with `@dnd-kit` |

### `AC.md` — Acceptance/implementation record
- Covers all 6 spec sections with concrete class/file names — good traceability.
- Describes the bug RCA and fix strategy clearly (single calc path, null-pole handling, dedup, 2-dp rounding).
- **Gap:** contains no test results, no coverage numbers, no links to `./logs` outputs — RULES §5/§6 require verification artifacts.

### `draft.md` — Working plan
- Solid 7-phase plan with named risks (JBehave JUnit-4 runners vs `useJUnitPlatform()`, Playwright browser binaries, network dependency).
- **Inconsistency:** contains *two competing plans* — the phase 0–7 plan and a second "__Plan (7 phases)__" (P1–P7) with **different artifact filenames** (`bug-coordinates-instability.md` vs spec's `bug_report_coordinates.md`; `iteration-02-backlog.md` vs `user_story_acceptance.md`; `data/history/<sessionId>.json` vs `task/history/result_*.json`). Spec filenames must win.
- States "nothing exists yet" and "baseline scoring unchanged" — contradicts `AC.md`'s completed claims; one of the two is stale.

### `TODO.md`
- 12 loose bullets, no priority, no traceability to spec sections, duplicates `draft.md` content.
- Items 1–3/5–12 map to spec §1–§6 but are not marked done despite `AC.md` claiming completion.

### `sprint/PI1.md`
- Accurate one-line-per-area summary; consistent with `BDD.md`. No estimates/DoD — acceptable as a sprint goal sheet.

---

## 2. Consistency Findings

1. **State contradiction:** `AC.md` (done) vs `draft.md` §"What I verified (nothing exists yet)". Must reconcile — mark `draft.md` phases as completed or move it to `task/done/`.
2. **Artifact path drift:** spec mandates `/task/user_story_acceptance.md`, `/task/bug_report_coordinates.md`, `/task/bdd_specifications.md`; `draft.md` alternative names violate spec §4.
3. **Class name drift:** spec says `ScoringService`, code is `ArchetypeScoringService` — documented, but spec file was not amended.
4. **Duplicate TODOs:** root `TODO.md` and `task/current/TODO.md` overlap; no single source of truth.

---

## 3. RULES.md Compliance Check

| Rule | Status |
|---|---|
| No `gradlew` / `git` execution | ✅ observed this session |
| No `//` comments (except `//TODO`) | ⚠️ unverified in code |
| `PROGRESS.md` pre-edit logging | ⚠️ no recent entries observed |
| Commit message + step in `artifact_pi.md` | ⚠️ unverified |
| Logs to `./logs` + `analyze_logs.sh` | ⚠️ `logs/` exists, output unverified |
| `//TODO` → `TODO.md` | ✅ `TODO.md` present, but not formatted as task/question pairs |

---

## 4. Verification Checklist (pending)

- [ ] Existence of `task/user_story_acceptance.md`, `task/bug_report_coordinates.md`, `task/bdd_specifications.md`
- [ ] Existence of `task/history/result_*.json` with identical-hash determinism pairs
- [ ] `./gradlew build` and `./gradlew e2eTest` results recorded (via CI/user — agent forbidden to run)
- [ ] `npm run build` for `/board`
- [ ] Coverage report for `ArchetypeScoringService` (TODO item 4)
- [ ] `//`-comment audit across `rest/` and `ui/src/`

---

## 5. Recommendations

1. Reconcile `draft.md` vs `AC.md` state; delete or archive the superseded P1–P7 plan block.
2. Enforce spec filenames verbatim for all `/task/*.md` artifacts.
3. Restructure `TODO.md` as checkboxes with spec-section tags (`§1`…`§6`) and mark completed items.
4. Append test/build evidence and the conventional commit artifact to `artifact_pi.md` per RULES §5.
5. Amend the spec's `ScoringService` name to `ArchetypeScoringService` (or alias note) to stop drift.
