# TODO: Collective Unconscious Engine

## Этап 1. Инфраструктура
- [ ] Настроить `build.gradle`: Java 25 + preview features, Spring Boot 3.4+, Lombok-свободный стиль, JUnit 5
- [ ] Проверить `settings.gradle` и структуру `src/main/java/com/unconscious.collective`
- [ ] Добавить `application.yml` (порт, виртуальные потоки, логирование)

## Этап 2. Domain-слой
- [ ] `domain/value/Vector3D` — record с `Math.clamp(-1.0, 1.0)` по каждой оси
- [ ] `domain/value/Axis`, `Sign` — типизированные полюса осей X/Y/Z
- [ ] `domain/octant/Octant` — enum из 8 октантов + `from(Vector3D)` (threshold 0.0)
- [ ] `domain/archetype/Archetype` — sealed interface: `PersonaArchetype`, `NarrativeArchetype`
- [ ] `domain/archetype/Shadow` — деструктивное проявление архетипа
- [ ] `domain/drive/CoreDrive` — sealed: `STABILITY`, `INDEPENDENCE`, `TRANSFORMATION`, `BELONGING`
- [ ] `domain/symbol/Symbol`, `SymbolCategory`
- [ ] `domain/model/SemanticObject`, `AnalysisResult`, `AssessmentPayload`
- [ ] Доменное исключение `DomainValidationException`

## Этап 3. Сервисы
- [ ] `OctantResolver` — координаты → `Octant` (использует record patterns)
- [ ] `QuizScoringService` — ответы теста по 3 осям → `Vector3D`
- [ ] `ArchetypeAnalyzer` — 5-шаговый алгоритм: фильтрация фактуры → мотив → конфликт → координаты → Тень
- [ ] `SemanticMatchingService` — сопоставление смысловых структур, дистанция/сходство

## Этап 4. Repository
- [x] SQL-схема `task/schema.sql` (PostgreSQL, справочники + seed-данные, CHECK-ограничения [-1,1])
- [ ] Интерфейсы: `ArchetypeRepository`, `SymbolRepository`, `AnalysisRepository` (ISP/DIP)
- [ ] In-memory реализации (`Map`-based, immutable snapshots)
- [ ] JPA-маппинг по `schema.sql` (при подключении персистентности)

## Этап 5. API-слой
- [ ] DTO-records для запросов/ответов
- [ ] `AnalysisController`, `QuizController`
- [ ] `ApiExceptionHandler` — `@RestControllerAdvice`, маппинг доменных ошибок

## Этап 6. Тесты и качество
- [ ] Юнит-тесты: `OctantResolver` (все 8 октантов + границы clamp)
- [ ] Юнит-тесты: `QuizScoringService` (счёт, пустой ввод, вне диапазона)
- [ ] Юнит-тесты: `ArchetypeAnalyzer` (корректная Тень, нарративная траектория)
- [ ] Юнит-тесты: `SemanticMatchingService`
- [ ] Интеграционный smoke-тест контроллеров (`@WebMvcTest` / MockMvc)

## Этап 6.1. UI
- [x] Спецификация UI `task/ui.md` (5 экранов, токены, вайрфреймы, дерево компонентов, API-контракт)
- [ ] Фронтенд-каркас: Welcome, Quiz, Result, Space, Analysis (React/TS или SPA-роутинг)
- [ ] 3D-сцена смыслового пространства (three.js + fallback SVG)
- [ ] Состояния loading/error/retry, прогресс квиза в sessionStorage
- [ ] Доступность: клавиатура, ARIA, контраст ≥ 4.5:1

## Этап 7. Финальная верификация
- [ ] `gradlew build` — зелёный прогон
- [ ] Проверка отсутствия комментариев `//` во всём `src/`
- [ ] Проверка: immutable state, отсутствие дублирования (DRY), нет лишних абстракций (YAGNI)
- [ ] Обновить `plan.md`/`impl.md` при расхождении реализации с планом
</parameter>
<task_progress>
- [x] Read all md files in task/ and summarize
- [x] Create task/plan.md
- [x] Create task/todo.md
- [ ] Create task/impl.md
</task_progress>
</write_to_file>