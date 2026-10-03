# Implementation: Collective Unconscious Engine (каркас)

Эталонные заготовки кода. Без комментариев `//`, immutable state, Java 25.

SQL-схема персистентности с seed-данными: `task/schema.sql`.

## Структура проекта

```
src/main/java/com/unconscious.collective
├── domain
│   ├── value/Vector3D.java
│   ├── value/Axis.java
│   ├── octant/Octant.java
│   ├── archetype/Archetype.java
│   ├── archetype/PersonaArchetype.java
│   ├── archetype/NarrativeArchetype.java
│   ├── archetype/Shadow.java
│   ├── drive/CoreDrive.java
│   ├── symbol/Symbol.java
│   ├── model/SemanticObject.java
│   ├── model/AnalysisResult.java
│   └── exception/DomainValidationException.java
├── service
│   ├── OctantResolver.java
│   ├── QuizScoringService.java
│   ├── ArchetypeAnalyzer.java
│   └── SemanticMatchingService.java
├── repository
│   ├── ArchetypeRepository.java
│   ├── SymbolRepository.java
│   ├── AnalysisRepository.java
│   └── inmemory/InMemoryAnalysisRepository.java
└── api
    ├── AnalysisController.java
    ├── QuizController.java
    ├── dto/AnalyzeRequest.java
    ├── dto/AnalysisResponse.java
    └── ApiExceptionHandler.java
```

## Domain

### Vector3D

```java
package com.unconscious.collective.domain.value;

public record Vector3D(double x, double y, double z) {

    private static final double MIN = -1.0;
    private static final double MAX = 1.0;

    public Vector3D {
        x = Math.clamp(x, MIN, MAX);
        y = Math.clamp(y, MIN, MAX);
        z = Math.clamp(z, MIN, MAX);
    }

    public static Vector3D origin() {
        return new Vector3D(0.0, 0.0, 0.0);
    }

    public double distanceTo(Vector3D other) {
        return Math.sqrt(
                Math.pow(x - other.x, 2)
                        + Math.pow(y - other.y, 2)
                        + Math.pow(z - other.z, 2));
    }
}
```

### Octant

```java
package com.unconscious.collective.domain.octant;

import com.unconscious.collective.domain.value.Vector3D;

import java.util.Arrays;
import java.util.Objects;

public enum Octant {
    SEEKER_INNOVATOR(-1, 1, 1, "Искатель / Инноватор"),
    REBEL_PIONEER(-1, 1, -1, "Бунтарь / Первопроходец"),
    SAGE_ANALYST(-1, -1, 1, "Мудрец / Аналитик"),
    MASTER_PRAGMATIST(-1, -1, -1, "Мастер / Прагматик"),
    PROPHET_IDEOLOGUE(1, 1, 1, "Пророк / Идеолог"),
    LEADER_REFORMER(1, 1, -1, "Вождь / Реформатор"),
    GUARDIAN_LEADER(1, -1, 1, "Хранитель / Духовный лидер"),
    CAREGIVER_EVERYMAN(1, -1, -1, "Опекун / Обыватель");

    private static final double NEUTRAL_THRESHOLD = 0.0;

    private final int signX;
    private final int signY;
    private final int signZ;
    private final String title;

    Octant(int signX, int signY, int signZ, String title) {
        this.signX = signX;
        this.signY = signY;
        this.signZ = signZ;
        this.title = title;
    }

    public static Octant from(Vector3D vector) {
        Objects.requireNonNull(vector, "vector must not be null");
        return Arrays.stream(values())
                .filter(octant -> octant.matches(vector))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no octant for " + vector));
    }

    private boolean matches(Vector3D vector) {
        return sign(vector.x()) == signX
                && sign(vector.y()) == signY
                && sign(vector.z()) == signZ;
    }

    private static int sign(double value) {
        return value > NEUTRAL_THRESHOLD ? 1 : -1;
    }

    public String title() {
        return title;
    }
}
```

### Archetype (sealed)

```java
package com.unconscious.collective.domain.archetype;

public sealed interface Archetype
        permits PersonaArchetype, NarrativeArchetype {

    String code();

    Shadow shadow();
}
```

```java
package com.unconscious.collective.domain.archetype;

public record Shadow(String description) {
}
```

### CoreDrive

```java
package com.unconscious.collective.domain.drive;

public enum CoreDrive {
    STABILITY("Стабильность и контроль"),
    INDEPENDENCE("Независимость и истина"),
    TRANSFORMATION("Изменение и риск"),
    BELONGING("Принадлежность и связь");

    private final String title;

    CoreDrive(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
```

### PersonaArchetype

```java
package com.unconscious.collective.domain.archetype;

import com.unconscious.collective.domain.drive.CoreDrive;

public enum PersonaArchetype implements Archetype {
    HERO("Герой", CoreDrive.TRANSFORMATION, new Shadow("Агрессор, гордец, не умеющий отступить")),
    SAGE("Мудрец", CoreDrive.INDEPENDENCE, new Shadow("Догматик, оторванный от реальности теоретик")),
    RULER("Правитель", CoreDrive.STABILITY, new Shadow("Тиран, бюрократ, контролер-параноик")),
    CREATOR("Творец", CoreDrive.STABILITY, new Shadow("Перфекционист, созидание ради процесса")),
    REBEL("Бунтарь", CoreDrive.TRANSFORMATION, new Shadow("Бессмысленное разрушение, авантюризм")),
    SEEKER("Искатель", CoreDrive.INDEPENDENCE, new Shadow("Изоляция, эгоцентризм")),
    CAREGIVER("Опекун", CoreDrive.BELONGING, new Shadow("Потеря индивидуальности, зависимость от чужого мнения")),
    JESTER("Шут", CoreDrive.BELONGING, new Shadow("Цинизм, отсутствие долгосрочного видения"));

    private final String title;
    private final CoreDrive drive;
    private final Shadow shadow;

    PersonaArchetype(String title, CoreDrive drive, Shadow shadow) {
        this.title = title;
        this.drive = drive;
        this.shadow = shadow;
    }

    @Override
    public String code() {
        return title;
    }

    @Override
    public Shadow shadow() {
        return shadow;
    }

    public CoreDrive drive() {
        return drive;
    }
}
```

### NarrativeArchetype

```java
package com.unconscious.collective.domain.archetype;

public enum NarrativeArchetype implements Archetype {
    JOURNEY("Путь / Путешествие", new Shadow("Блуждание без цели")),
    DEATH_REBIRTH("Смерть и Возрождение", new Shadow("Зацикленность на разрушении")),
    ORDER_VS_CHAOS("Борьба Порядка и Хаоса", new Shadow("Тоталитарный контроль")),
    FALL("Падение", new Shadow("Саморазрушение, утрата смысла"));

    private final String title;
    private final Shadow shadow;

    NarrativeArchetype(String title, Shadow shadow) {
        this.title = title;
        this.shadow = shadow;
    }

    @Override
    public String code() {
        return title;
    }

    @Override
    public Shadow shadow() {
        return shadow;
    }
}
```

### Symbol и модели

```java
package com.unconscious.collective.domain.symbol;

import com.unconscious.collective.domain.value.Vector3D;

import java.util.Objects;

public record Symbol(String name, SymbolCategory category, Vector3D position) {

    public Symbol {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(category, "category must not be null");
        Objects.requireNonNull(position, "position must not be null");
    }
}
```

```java
package com.unconscious.collective.domain.symbol;

public enum SymbolCategory {
    PERSONA,
    SHADOW,
    ANIMA_ANIMUS,
    SELF,
    WORLD_TREE,
    GREAT_MOTHER,
    HERO
}
```

```java
package com.unconscious.collective.domain.model;

import com.unconscious.collective.domain.drive.CoreDrive;
import com.unconscious.collective.domain.value.Vector3D;

import java.util.Objects;

public record SemanticObject(String id, String essence, Vector3D position, CoreDrive drive) {

    public SemanticObject {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(essence, "essence must not be null");
        Objects.requireNonNull(position, "position must not be null");
        Objects.requireNonNull(drive, "drive must not be null");
    }
}
```

```java
package com.unconscious.collective.domain.model;

import com.unconscious.collective.domain.archetype.Archetype;
import com.unconscious.collective.domain.archetype.NarrativeArchetype;
import com.unconscious.collective.domain.drive.CoreDrive;
import com.unconscious.collective.domain.octant.Octant;
import com.unconscious.collective.domain.value.Vector3D;

public record AnalysisResult(
        String objectId,
        Vector3D position,
        Octant octant,
        Archetype dominant,
        NarrativeArchetype trajectory,
        String shadowManifestation,
        CoreDrive drive) {
}
```

```java
package com.unconscious.collective.domain.exception;

public class DomainValidationException extends RuntimeException {

    public DomainValidationException(String message) {
        super(message);
    }
}
```

## Сервисы

### QuizScoringService

```java
package com.unconscious.collective.service;

import com.unconscious.collective.domain.exception.DomainValidationException;
import com.unconscious.collective.domain.value.Vector3D;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizScoringService {

    public Vector3D score(List<Integer> socialAnswers, List<Integer> changeAnswers, List<Integer> knowledgeAnswers) {
        return new Vector3D(
                normalize(socialAnswers, "social"),
                normalize(changeAnswers, "change"),
                normalize(knowledgeAnswers, "knowledge"));
    }

    private double normalize(List<Integer> answers, String axis) {
        if (answers == null || answers.isEmpty()) {
            throw new DomainValidationException("answers for axis '" + axis + "' must not be empty");
        }
        double sum = answers.stream()
                .mapToInt(Integer::intValue)
                .sum();
        double max = answers.size() * 2.0;
        return (sum / max) * 2.0 - 1.0;
    }
}
```

### ArchetypeAnalyzer

```java
package com.unconscious.collective.service;

import com.unconscious.collective.domain.archetype.NarrativeArchetype;
import com.unconscious.collective.domain.archetype.PersonaArchetype;
import com.unconscious.collective.domain.model.AnalysisResult;
import com.unconscious.collective.domain.model.SemanticObject;
import com.unconscious.collective.domain.octant.Octant;
import org.springframework.stereotype.Service;

@Service
public class ArchetypeAnalyzer {

    private final OctantResolver octantResolver;

    public ArchetypeAnalyzer(OctantResolver octantResolver) {
        this.octantResolver = octantResolver;
    }

    public AnalysisResult analyze(SemanticObject object) {
        Octant octant = octantResolver.resolve(object.position());
        PersonaArchetype persona = mapPersona(object, octant);
        NarrativeArchetype trajectory = mapTrajectory(object);
        return new AnalysisResult(
                object.id(),
                object.position(),
                octant,
                persona,
                trajectory,
                persona.shadow().description(),
                object.drive());
    }

    private PersonaArchetype mapPersona(SemanticObject object, Octant octant) {
        return switch (object.drive()) {
            case STABILITY -> octant == Octant.LEADER_REFORMER || octant == Octant.PROPHET_IDEOLOGUE
                    ? PersonaArchetype.RULER
                    : PersonaArchetype.CREATOR;
            case INDEPENDENCE -> octant == Octant.SAGE_ANALYST
                    ? PersonaArchetype.SAGE
                    : PersonaArchetype.SEEKER;
            case TRANSFORMATION -> octant == Octant.REBEL_PIONEER
                    ? PersonaArchetype.REBEL
                    : PersonaArchetype.HERO;
            case BELONGING -> octant == Octant.CAREGIVER_EVERYMAN
                    ? PersonaArchetype.CAREGIVER
                    : PersonaArchetype.JESTER;
        };
    }

    private NarrativeArchetype mapTrajectory(SemanticObject object) {
        return switch (object.drive()) {
            case STABILITY -> NarrativeArchetype.ORDER_VS_CHAOS;
            case TRANSFORMATION -> NarrativeArchetype.DEATH_REBIRTH;
            case INDEPENDENCE -> NarrativeArchetype.JOURNEY;
            case BELONGING -> NarrativeArchetype.FALL;
        };
    }
}
```

### SemanticMatchingService

```java
package com.unconscious.collective.service;

import com.unconscious.collective.domain.model.SemanticObject;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SemanticMatchingService {

    public List<ScoredMatch> findClosest(SemanticObject probe, List<SemanticObject> candidates, int limit) {
        if (limit < 1) {
            return List.of();
        }
        return candidates.stream()
                .filter(candidate -> !candidate.id().equals(probe.id()))
                .map(candidate -> new ScoredMatch(candidate, probe.position().distanceTo(candidate.position())))
                .sorted(Comparator.comparingDouble(ScoredMatch::distance))
                .limit(limit)
                .toList();
    }

    public record ScoredMatch(SemanticObject candidate, double distance) {
    }
}
```

## Repository

```java
package com.unconscious.collective.repository;

import com.unconscious.collective.domain.model.AnalysisResult;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository {

    AnalysisResult save(AnalysisResult result);

    Optional<AnalysisResult> findById(String objectId);

    List<AnalysisResult> findAll();
}
```

```java
package com.unconscious.collective.repository.inmemory;

import com.unconscious.collective.domain.exception.DomainValidationException;
import com.unconscious.collective.domain.model.AnalysisResult;
import com.unconscious.collective.repository.AnalysisRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAnalysisRepository implements AnalysisRepository {

    private final Map<String, AnalysisResult> storage = new ConcurrentHashMap<>();

    @Override
    public AnalysisResult save(AnalysisResult result) {
        storage.put(result.objectId(), result);
        return result;
    }

    @Override
    public Optional<AnalysisResult> findById(String objectId) {
        return Optional.ofNullable(storage.get(objectId));
    }

    @Override
    public List<AnalysisResult> findAll() {
        return List.copyOf(storage.values());
    }

    public AnalysisResult requireById(String objectId) {
        return findById(objectId)
                .orElseThrow(() -> new DomainValidationException("analysis not found: " + objectId));
    }
}
```

Аналогично узкие интерфейсы `ArchetypeRepository` и `SymbolRepository` (ISP).

## API

### DTO

```java
package com.unconscious.collective.api.dto;

import com.unconscious.collective.domain.drive.CoreDrive;

public record AnalyzeRequest(String id, String essence, double x, double y, double z, CoreDrive drive) {
}
```

```java
package com.unconscious.collective.api.dto;

import com.unconscious.collective.domain.drive.CoreDrive;
import com.unconscious.collective.domain.octant.Octant;

public record AnalysisResponse(
        String objectId,
        double x,
        double y,
        double z,
        Octant octant,
        String archetype,
        String trajectory,
        String shadow,
        CoreDrive drive) {
}
```

### AnalysisController

```java
package com.unconscious.collective.api;

import com.unconscious.collective.api.dto.AnalyzeRequest;
import com.unconscious.collective.api.dto.AnalysisResponse;
import com.unconscious.collective.domain.model.AnalysisResult;
import com.unconscious.collective.domain.model.SemanticObject;
import com.unconscious.collective.domain.value.Vector3D;
import com.unconscious.collective.repository.AnalysisRepository;
import com.unconscious.collective.service.ArchetypeAnalyzer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analysis")
public class AnalysisController {

    private final ArchetypeAnalyzer analyzer;
    private final AnalysisRepository repository;

    public AnalysisController(ArchetypeAnalyzer analyzer, AnalysisRepository repository) {
        this.analyzer = analyzer;
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalyzeRequest request) {
        SemanticObject object = new SemanticObject(
                request.id(),
                request.essence(),
                new Vector3D(request.x(), request.y(), request.z()),
                request.drive());
        AnalysisResult result = repository.save(analyzer.analyze(object));
        return ResponseEntity.ok(toResponse(result));
    }

    private AnalysisResponse toResponse(AnalysisResult result) {
        var position = result.position();
        return new AnalysisResponse(
                result.objectId(),
                position.x(),
                position.y(),
                position.z(),
                result.octant(),
                result.dominant().code(),
                result.trajectory().code(),
                result.shadowManifestation(),
                result.drive());
    }
}
```

### ApiExceptionHandler

```java
package com.unconscious.collective.api;

import com.unconscious.collective.domain.exception.DomainValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<Map<String, String>> handleDomain(DomainValidationException exception) {
        log.warn("domain validation failed: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("illegal argument: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}
```

## Тест (пример)

```java
package com.unconscious.collective.octant;

import com.unconscious.collective.domain.octant.Octant;
import com.unconscious.collective.domain.value.Vector3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OctantResolverTest {

    @Test
    void mapsAllEightOctants() {
        assertEquals(Octant.PROPHET_IDEOLOGUE, Octant.from(new Vector3D(0.5, 0.5, 0.5)));
        assertEquals(Octant.LEADER_REFORMER, Octant.from(new Vector3D(0.5, 0.5, -0.5)));
        assertEquals(Octant.GUARDIAN_LEADER, Octant.from(new Vector3D(0.5, -0.5, 0.5)));
        assertEquals(Octant.CAREGIVER_EVERYMAN, Octant.from(new Vector3D(0.5, -0.5, -0.5)));
        assertEquals(Octant.SEEKER_INNOVATOR, Octant.from(new Vector3D(-0.5, 0.5, 0.5)));
        assertEquals(Octant.REBEL_PIONEER, Octant.from(new Vector3D(-0.5, 0.5, -0.5)));
        assertEquals(Octant.SAGE_ANALYST, Octant.from(new Vector3D(-0.5, -0.5, 0.5)));
        assertEquals(Octant.MASTER_PRAGMATIST, Octant.from(new Vector3D(-0.5, -0.5, -0.5)));
    }

    @Test
    void clampsOutOfRangeCoordinates() {
        Vector3D vector = new Vector3D(5.0, -9.0, 0.0);
        assertEquals(1.0, vector.x());
        assertEquals(-1.0, vector.y());
        assertEquals(0.0, vector.z());
    }
}
```

## Пример ответа API

```json
{
  "objectId": "startup-garage",
  "x": -0.6,
  "y": 0.8,
  "z": 0.4,
  "octant": "SEEKER_INNOVATOR",
  "archetype": "Искатель",
  "trajectory": "Путь / Путешествие",
  "shadow": "Изоляция, эгоцентризм",
  "drive": "INDEPENDENCE"
}
```
