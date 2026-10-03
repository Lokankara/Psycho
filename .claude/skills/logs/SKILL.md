---
name: logs
description: Route, name and inspect project logs in the ./logs directory per task/RULES.md section 6 (*.log for output, *.err for errors) and analyse them with ./scripts/analyze_logs.sh --path=./logs, including Windows/Git-Bash notes and the locations of Gradle/Spring/Vite reports.
---

# Logs (`./logs`)

## Specification (`E:\pets\quiz\task\RULES.md` §6)

- Route **all system outputs, execution errors, and application runtime logs** to `E:\pets\quiz\logs`.
- Extensions: `*.log` for normal output, `*.err` for errors/stack traces.
- Provide the analysis command: `./scripts/analyze_logs.sh --path=./logs`
- §5/Post-Execution: after every step, **verify log outputs in `./logs`** and record the execution details plus the conventional commit message in `task/current/sprint/pi-1/artifacti_pi.md`.

Current state: `logs/` contains only `.gitkeep` (the directory exists but nothing is written there yet - flagged in `task/current/sprint/review.md`). `.gitignore` does **not** ignore `logs/`, so log files would be committed; prefer short-lived files and clean them with the commands below when they are no longer needed.

## Layout

```
E:\pets\quiz\
├── logs\
│   ├── .gitkeep                      # keeps the dir in git
│   ├── <source>-<yyyyMMdd_HHmmss>.log   # stdout / informational
│   └── <source>-<yyyyMMdd_HHmmss>.err   # stderr / failures
└── scripts\
    └── analyze_logs.sh               # parser for ./logs
```

Naming convention for new files: `<source>-<yyyyMMdd_HHmmss>.log` / `.err`, e.g. `gradle-build-20261004_101500.log`, `boot-run-20261004_101500.err`. One file per execution; never append unrelated runs into one file.

## Routing output

PowerShell (Windows, the environment here):

```powershell
# combined output + errors into one .log
.\gradlew.bat build --no-daemon *>&1 | Tee-Object -FilePath logs\gradle-build.log

# split streams: normal -> .log, errors -> .err
& .\gradlew.bat test --no-daemon 2> logs\gradle-test.err 1> logs\gradle-test.log
```

POSIX:

```bash
./gradlew build --no-daemon > logs/gradle-build.log 2> logs/gradle-build.err
```

Spring Boot runtime (no file appender is configured in `rest/src/main/resources/application.properties`, so the app logs to console only): capture the console, or set `logging.file.name=logs/boot-run.log` in `application.properties` if persistent file logging is required (`//TODO` + `task/current/sprint/pi-1/TODO.md` if the choice is unclear).

## Analysis

```bash
./scripts/analyze_logs.sh --path=./logs     # documented command (RULES.md §6)
```

The script (`E:\pets\quiz\scripts\analyze_logs.sh`, bash, `set -euo pipefail`) prints three sections:

1. **Files in ./logs** - `find` of `*.log` and `*.err` at max depth 1, sorted.
2. **Error lines** - every line matching `ERROR|Exception|Caused by|FAILED`, prefixed with the file name.
3. **Summary** - per-file count of those error lines.

Flags: `--path=<dir>` (default `./logs`), `-h|--help`. Exits 1 if the directory does not exist. Note the summary counter uses `grep -c`, so a line with two matches still counts once per line; the "no error lines found" fallback prints when nothing matches.

Windows: `bash` is required (Git Bash or WSL). `C:\Windows\system32\bash.exe` here is WSL - the repo is reachable inside WSL at `/mnt/e/pets/quiz`, so run:

```bash
bash /mnt/e/pets/quiz/scripts/analyze_logs.sh --path=/mnt/e/pets/quiz/logs
# or, from Git Bash with cwd E:\pets\quiz:
bash scripts/analyze_logs.sh --path=./logs
```

Equivalent quick check without bash (PowerShell):

```powershell
Get-ChildItem logs -Include *.log,*.err -Recurse |
  Select-String -Pattern 'ERROR|Exception|Caused by|FAILED'
```

## Related output locations (not in ./logs)

| Output | Location |
|--------|----------|
| Gradle test report (HTML) | `build\reports\tests\test\index.html`, `bdd\build\reports\tests\...` |
| Gradle test results (XML) | `build\test-results\test\*.xml` |
| Gradle problems report | `build\reports\problems\` |
| Vite build log | console of `:ui:npmBuild` (tee it into `logs/` if it must be kept) |
| Execution history JSON | `task/history/result_YYYYMMDD_HHMMSS.json` (written by `HistoryLogger`, separate from `./logs`) |

## Checklist per task (RULES.md §5-6)

1. Before editing: write planned steps to `PROGRESS.md`.
2. During: tee command output into `logs/<source>-<timestamp>.log` / `.err`.
3. After: run `./scripts/analyze_logs.sh --path=./logs`, confirm no unexpected `ERROR|Exception|FAILED` lines.
4. Record the step, test results, and conventional commit message in `artifacti_pi.md`.
