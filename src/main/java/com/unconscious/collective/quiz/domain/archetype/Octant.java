package com.unconscious.collective.quiz.domain.archetype;

import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Coordinates;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * One of the eight semantic octant produced by the intersection of the three axes.
 * Each octant carries its full archetypal profile
 * (meaning, dominant archetypes, narrative trajectory and the "Shadow" form).
 */
public enum Octant implements Serializable {

    GUARDIAN_LEADER(true, false, true,
            "Рефлексия (Осознание)",
            "Хранитель / Духовный лидер",
            "Внутреннее структурное понимание; самосознание, формирование ценностей, ясные смыслы.",
            "Хранитель, Опекун, Мудрец",
            EnumSet.of(StructuralArchetype.SELF, StructuralArchetype.PERSONA),
            "Порядок и систематизация смыслов — сохранение и передача традиции.",
            "Застой, догматизм, духовная гордыня, закостенелые идеалы."),

    PROPHET_IDEOLOGUE(true, true, true,
            "Переживание (Аффект)",
            "Пророк / Идеолог",
            "Внутренний поток состояний; эмоции, интуиция, бессознательные процессы, чувства.",
            "Пророк, Любовник, Опекун",
            EnumSet.of(StructuralArchetype.ANIMA, StructuralArchetype.ANIMUS),
            "Смерть и Возрождение — коллективная трансформация через общий смысл.",
            "Фанатизм, манипуляция массами, догматическая нетерпимость."),

    CAREGIVER_EVERYMAN(true, false, false,
            "Воля (Установка)",
            "Опекун / Обыватель",
            "Внутреннее намерение и решение; принятие выбора, самодисциплина, фокус внимания.",
            "Опекун, Славный малый, Обыватель",
            EnumSet.of(StructuralArchetype.PERSONA, StructuralArchetype.EGO),
            "Систематизация: беспорядок → анализ → правила → устойчивый порядок.",
            "Конформизм, патернализм, растворение индивидуальности."),

    LEADER_REFORMER(true, true, false,
            "Творчество (Экспрессия)",
            "Вождь / Реформатор",
            "Внутренняя непрерывная активность; воображение, импровизация, поток, самовыражение.",
            "Вождь, Герой, Славный малый",
            EnumSet.of(StructuralArchetype.EGO, StructuralArchetype.ANIMUS),
            "Путь / Трансформация: вызов → кризис → преодоление → новое качество.",
            "Демагогия, авантюризм, разрушение ради перемен."),

    SAGE_ANALYST(false, false, true,
            "Фактология (Знание)",
            "Мудрец / Аналитик",
            "Внешняя фиксированная данность; законы природы, документы, архивы, объективные данные.",
            "Мудрец, Аналитик, Хранитель",
            EnumSet.of(StructuralArchetype.SELF, StructuralArchetype.ANIMUS),
            "Систематизация знания: анализ → выработка правил → порядок.",
            "Бесплодный скептицизм, отрыв от практики, информационная зависимость."),

    SEEKER_INNOVATOR(false, true, true,
            "Наблюдение (Феномен)",
            "Искатель / Инноватор",
            "Внешние процессы в реальном времени; изменения среды, тренды, естественные явления.",
            "Искатель, Маг, Бунтарь",
            EnumSet.of(StructuralArchetype.EGO, StructuralArchetype.ANIMA),
            "Путь: известное → неизвестное → возвращение с обновлённым смыслом.",
            "Вечный поиск без завершения, рассеянность, авантюризм."),

    MASTER_PRAGMATIST(false, false, false,
            "Конструирование (Инженерия)",
            "Мастер / Прагматик",
            "Внешнее структурное воздействие; создание институтов, систем, зданий, алгоритмов.",
            "Мастер, Изгой, Воин",
            EnumSet.of(StructuralArchetype.ANIMUS, StructuralArchetype.EGO),
            "Созидание: стагнация → слом устаревшего → освобождение → новая форма.",
            "Цинизм, примитивизм, отсутствие долгосрочного видения."),

    REBEL_PIONEER(false, true, false,
            "Преобразование (Энергия)",
            "Бунтарь / Первопроходец",
            "Внешнее прямое физическое воздействие; энергия, сила, преобразование материи.",
            "Бунтарь, Воин, Герой",
            EnumSet.of(StructuralArchetype.SHADOW, StructuralArchetype.PERSONA, StructuralArchetype.ANIMUS),
            "Смерть и Возрождение: слом системы → освобождение → новая реальность.",
            "Бессмысленное разрушение, насилие, хаос ради хаоса.");

    private final boolean xPositive;
    private final boolean yPositive;
    private final boolean zPositive;
    private final String octantName;
    private final String archetypeName;
    private final String meaning;
    private final String dominantArchetypes;
    private final Set<StructuralArchetype> structuralArchetypes;
    private final String narrativeTrajectory;
    private final String shadow;

    Octant(boolean xPositive,
           boolean yPositive,
           boolean zPositive,
           String octantName,
           String archetypeName,
           String meaning,
           String dominantArchetypes,
           Set<StructuralArchetype> structuralArchetypes,
           String narrativeTrajectory,
           String shadow) {
        this.xPositive = xPositive;
        this.yPositive = yPositive;
        this.zPositive = zPositive;
        this.octantName = octantName;
        this.archetypeName = archetypeName;
        this.meaning = meaning;
        this.dominantArchetypes = dominantArchetypes;
        this.structuralArchetypes = structuralArchetypes;
        this.narrativeTrajectory = narrativeTrajectory;
        this.shadow = shadow;
    }

    public static Octant from(Coordinates coordinates) {
        return fromCoordinates(
                coordinates.x() >= 0,
                coordinates.y() >= 0,
                coordinates.z() >= 0
        );
    }

    public boolean isPositive(Axis axis) {
        return switch (axis) {
            case X -> this.xPositive;
            case Y -> this.yPositive;
            case Z -> this.zPositive;
        };
    }

    public static Octant fromCoordinates(boolean xPositive, boolean yPositive, boolean zPositive) {
        return Arrays.stream(values())
                .filter(o -> o.xPositive == xPositive
                        && o.yPositive == yPositive
                        && o.zPositive == zPositive)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid coordinate combination: " + xPositive + ", " + yPositive + ", " + zPositive));
    }

    public String octantName() {
        return octantName;
    }

    public String archetypeName() {
        return archetypeName;
    }

    public String meaning() {
        return meaning;
    }

    public String dominantArchetypes() {
        return dominantArchetypes;
    }

    public Set<StructuralArchetype> structuralArchetypes() {
        return structuralArchetypes;
    }

    public String narrativeTrajectory() {
        return narrativeTrajectory;
    }

    public String shadow() {
        return shadow;
    }

    /**
     * Human-readable sign pattern of this octant, e.g. {@code (+, −, +)}.
     */
    public String coordinateLabel() {
        return "(" + sign(xPositive) + ", " + sign(yPositive) + ", " + sign(zPositive) + ")";
    }

    private static String sign(boolean positive) {
        return positive ? "+" : "−";
    }
}
