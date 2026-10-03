# Plan: Collective Unconscious Engine

## 1. Цель
Разработать архитектуру и базовый каркас приложения «Collective Unconscious Engine» на Java 25 — сервиса моделирования, анализа и количественной оценки психологических и семантических архетипов в непрерывном 3D-пространстве векторов $(X, Y, Z)$.

Требования: SOLID, DRY, KISS, YAGNI, modern Java (records, pattern matching, streams), immutable state, корректная обработка ошибок, **без комментариев `//`**, разделение ответственности domain / service / repository / api.

## 2. Единое соглашение по осям (зафиксировано)
Источник истины — `project_architecture_plan.md` (знаки `desc.md`):

| Ось | Полюс − | Полюс + |
|---|---|---|
| $X$ — Социальный вектор | Индивидуализм / Автономия | Коллективизм / Принадлежность |
| $Y$ — Вектор изменений | Стабильность / Контроль | Трансформация / Риск |
| $Z$ — Понятийный вектор | Материализм / Эмпирика | Абстракция / Трансцендентность |

- Координаты — вещественные числа в диапазоне `[-1.0, 1.0]`, ограничение через `Math.clamp()`.
- `SYSTEM.md` используется как промпт для LLM-вызова анализатора; его числовая нумерация октантов и инвертированный знак $X$ не переносятся в код.

### 8 архетипных октантов
| Координаты | Enum | Профиль |
|---|---|---|
| $(-X, +Y, +Z)$ | `SEEKER_INNOVATOR` | Искатель / Инноватор |
| $(-X, +Y, -Z)$ | `REBEL_PIONEER` | Бунтарь / Первопроходец |
| $(-X, -Y, +Z)$ | `SAGE_ANALYST` | Мудрец / Аналитик |
| $(-X, -Y, -Z)$ | `MASTER_PRAGMATIST` | Мастер / Прагматик |
| $(+X, +Y, +Z)$ | `PROPHET_IDEOLOGUE` | Пророк / Идеолог |
| $(+X, +Y, -Z)$ | `LEADER_REFORMER` | Вождь / Реформатор |
| $(+X, -Y, +Z)$ | `GUARDIAN_LEADER` | Хранитель / Духовный лидер |
| $(+X, -Y, -Z)$ | `CAREGIVER_EVERYMAN` | Опекун / Обыватель |

## 3. Технический стек
- Java 25, preview features включены.
- Spring Boot 3.4+ / Spring 7, виртуальные потоки.
- Используемые фичи Java 25:
  - `ScopedValue` — контекст запроса через виртуальные потоки вместо `ThreadLocal`.
  - Record patterns и switch pattern matching — деконструация координат и payload-ответов.
  - `Math.clamp()` — жёсткие границы `[-1.0, 1.0]`.
  - Sealed interfaces — строгие типизированные иерархии `Archetype` и типов ответов квиза.

## 4. Структура пакетов
```
com.unconscious.collective
├── domain
│   ├── value        Vector3D (record, clamp), Sign, Axis
│   ├── archetype    Archetype (sealed), Shadow, NarrativeTrajectory, PersonaArchetype
│   ├── octant       Octant (enum, from(Vector3D))
│   ├── drive        CoreDrive (sealed), DrivePolarity
│   ├── symbol       Symbol, SymbolCategory
│   └── model        SemanticObject, AnalysisResult, AssessmentPayload
├── service
│   ├── OctantResolver           координаты → октант
│   ├── ArchetypeAnalyzer        5-шаговый алгоритм анализа
│   ├── QuizScoringService       ответы теста → точка (X, Y, Z)
│   └── SemanticMatchingService  сопоставление смысловых структур
├── repository (interfaces + inmemory)
│   ├── ArchetypeRepository
│   ├── SymbolRepository
│   └── AnalysisRepository
└── api
    ├── AnalysisController, QuizController
    ├── dto (records)
    └── ApiExceptionHandler
```

Персистентность: `task/schema.sql` — PostgreSQL-схема (справочники `core_drive`, `octant`, `persona_archetype`, `narrative_archetype`, `symbol_category`; транзакционные `semantic_object`, `analysis_result`, `quiz_session`, `quiz_answer`, `semantic_match`) с CHECK-ограничениями на диапазон `[-1.0, 1.0]` и seed-данными.

UI: `task/ui.md` — 5 экранов (Welcome, Quiz, Result, Space, Analysis), дизайн-токены, ASCII-вайрфреймы, компонентное дерево, контракт с API, доступность.

## 5. Ключевые проектные решения
1. **SRP**: каждый сервис — одна ответственность; репозитории не содержат бизнес-логики.
2. **OCP/ДIP**: сервисы зависят от интерфейсов репозиториев; новые октанты/архетипы добавляются через enum/sealed-иерархию без изменения resolver-логики.
3. **ISP**: три узких репозиторных интерфейса вместо монолитного.
4. **LSP**: in-memory реализации подменяют потенциальные персистентные без изменения потребителей.
5. **Иммутабельность**: все domain-типы — records / unmodifiable collections.
6. **Ошибки**: доменные исключения (`DomainValidationException`) + `@RestControllerAdvice`, логирование SLF4J.
7. **YAGNI**: нет абстракций «на будущее» — только каркас, требуемый ТЗ.

## 6. Алгоритм анализа (переносится в `ArchetypeAnalyzer`)
1. Фильтрация фактуры (снятие декораций).
2. Анализ мотивов и целей (базовая потребность → `CoreDrive`).
3. Анализ структуры конфликта (бинарная оппозиция).
4. Позиционирование: координаты $(X, Y, Z)$ + октант.
5. Валидация через «Тень» (деструктивное проявление архетипа).

## 7. Этапы реализации
1. `build.gradle`: Java 25 + preview, Spring Boot, JUnit.
2. Domain-layer типы.
3. `OctantResolver` + `QuizScoringService` + тесты.
4. `ArchetypeAnalyzer`, `SemanticMatchingService`.
5. Репозитории (in-memory).
6. API-слой (controller, DTO, exception handler).
7. `gradlew build` — зелёный прогон, проверка отсутствия `//`.
</parameter>
<task_progress>
- [x] Read all md files in task/ and summarize
- [ ] Create task/plan.md
- [ ] Create task/todo.md
- [ ] Create task/impl.md
</task_progress>
</write_to_file>