---
name: gradle
description: Run and reason about the Gradle 9.7.1 multi-module build of the quiz project (modules rest, ui, bdd, kanban) - wrapper commands, module tasks, Java 25 toolchains, CI parity, output locations and the repo rule that forbids the agent from executing gradlew.
---

# Gradle (quiz build)

## Environment

| Item | Value |
|------|-------|
| Wrapper | `E:\pets\quiz\gradlew` (bash) / `gradlew.bat` (Windows PowerShell) |
| Gradle version | 9.7.1 (`gradle/wrapper/gradle-wrapper.properties`) |
| Java | toolchain `JavaLanguageVersion.of(25)` in `rest/build.gradle` and `bdd/build.gradle`; CI uses Temurin 25 |
| Settings | `settings.gradle`: `rootProject.name = 'quiz'`, includes `rest`, `ui`, `bdd`, `kanban` |
| Root build file | `E:\pets\quiz\build.gradle` - only `group`/`version`, no plugins |
| OS here | Windows (`win32`), shell PowerShell |

## Modules and their tasks

| Module | Build file | Plugins | Notable tasks |
|--------|-----------|---------|---------------|
| `rest` | `E:\pets\quiz\rest\build.gradle` | `java`, `org.springframework.boot` 4.1.1, `io.spring.dependency-management` 1.1.7 | `bootRun`, `bootJar` (used by `Dockerfile`), `test` (JUnit Platform); `processResources` dependsOn `:ui:npmBuild` and copies `ui/dist` into `static` |
| `bdd` | `E:\pets\quiz\bdd\build.gradle` | same Spring Boot trio | `test`, custom `e2eTest` (`group = 'verification'`, JBehave via JUnit Platform, includes `**/*E2ETest.class`); `bootJar`/`bootBuildImage` disabled |
| `ui` | `E:\pets\quiz\ui\build.gradle` | `base`, `com.github.node-gradle.node` 7.1.0 | `npmBuild` (runs `npm run build` = vite build), wired into `assemble`; Node 22.11.0 / npm 10.9.0 downloaded into `ui/build/nodejs`, `ui/build/npm` |
| `kanban` | none | none | Declared in `settings.gradle` but has no `build.gradle`; sources live in `E:\pets\quiz\kanban\src` (TypeScript/React) and are not built by Gradle |

## Commands

Run from `E:\pets\quiz`:

```powershell
# Windows (PowerShell)
.\gradlew.bat build --no-daemon
.\gradlew.bat test --no-daemon
.\gradlew.bat :bdd:e2eTest
.\gradlew.bat :rest:bootRun
.\gradlew.bat :rest:bootJar --no-daemon
.\gradlew.bat clean build
.\gradlew.bat tasks --all
```

```bash
# POSIX / CI (.github/workflows/ci.yml, Dockerfile)
./gradlew build --no-daemon      # CI "Build with Gradle"
./gradlew test --no-daemon       # CI "Run tests"
./gradlew :rest:bootJar --no-daemon   # Docker build stage
chmod +x gradlew                 # required on fresh CI checkouts
```

Always prefer `--no-daemon` for CI-like or one-shot runs (matches `.github/workflows/ci.yml` and `Dockerfile`).

## Hard rules (from `E:\pets\quiz\task\RULES.md`)

- **§2: DO NOT execute any `gradlew` commands** as the agent, and **DO NOT execute any `git` commands**. Produce the exact command for the user/CI to run, then record the result in `PROGRESS.md` / `artifact_pi.md` when it comes back. The commands above are documentation and hand-off artifacts, not things to run yourself.
- **§3: no `//` comments in code** (only `//TODO` markers for unknowns, mirrored into `task/current/sprint/pi-1/TODO.md`).
- **§6:** route command output and runtime logs to `./logs` (`*.log`, `*.err`) - see the `logs` skill.

## Where build outputs land

- Test HTML report: `E:\pets\quiz\build\reports\tests\test\index.html` (and `bdd\build\reports\tests\...`)
- Test XML results: `E:\pets\quiz\build\test-results\test\*.xml`
- Compiled classes/jars: `build\classes`, `build\libs`, `rest\build\libs\*.jar`
- UI bundle: `ui\dist` (gitignored) copied into `rest` resources as `static`
- All of `build/`, `.gradle`, `ui/dist`, `ui/node_modules` are gitignored (`.gitignore`)

## Troubleshooting

- Stuck `bootRun` / leftover JVM: `taskkill /F /IM java.exe /T` (from `README.md`).
- `ui:npmBuild` failing: run inside `E:\pets\quiz\ui` - `npm run typecheck` (`tsc --noEmit`) and `npm run build`.
- Plugin/dependency resolution needs network (Spring Boot 4.1.1, node-gradle 7.1.0); there is no local maven repo mirror configured.
- Local Gradle installs also exist (`e:\distr\gradle-8.0.2`), but 8.0.2 is older than the wrapper - always use the wrapper for real builds.
