package com.unconscious.collective.quiz.domain.archetype;

import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;

/**
 * A fundamental driving motive, anchored to one pole of one axis.
 */
public enum CoreDriveType {

    STABILITY_AND_CONTROL("Стабильность и контроль",
            "Создание структуры, наведение порядка, управление рисками.", Axis.Y, Pole.NEGATIVE),
    INDEPENDENCE_AND_TRUTH("Независимость и истина",
            "Поиск знания, исследование неизвестного, автономия.", Axis.X, Pole.NEGATIVE),
    CHANGE_AND_RISK("Изменение и риск",
            "Разрушение устаревшего, преодоление, трансформация.", Axis.Y, Pole.POSITIVE),
    BELONGING_AND_CONNECTION("Принадлежность и связь",
            "Установление контакта, сопричастность, социальная интеграция.", Axis.X, Pole.POSITIVE),
    MATERIAL_PRAGMATISM("Польза и результат",
            "Сенсорный опыт, прагматизм, мгновенная польза.", Axis.Z, Pole.NEGATIVE),
    ABSTRACT_MEANING("Смысл и понимание",
            "Поиск фундаментальных законов, концептуализация, метафизика.", Axis.Z, Pole.POSITIVE),
    SURVIVAL_AND_SAFETY("Выживание и безопасность", "Сохранение жизни, физиологический комфорт, избегание угроз.", null, Pole.NEGATIVE),
    POWER_AND_DOMINANCE("Власть и доминирование", "Расширение влияния, иерархический контроль, победа.", null, Pole.POSITIVE),
    EXPRESSION_AND_CREATION("Самовыражение и созидание", "Воплощение внутреннего мира, эстетика, творческий поток.", null, Pole.POSITIVE),
    PLAY_AND_HEDONISM("Игра и удовольствие", "Спонтанность, радость момента, получение положительных эмоций.", null, Pole.POSITIVE);

    private final String label;
    private final String description;
    private final Axis axis;
    private final Pole pole;

    CoreDriveType(String label, String description, Axis axis, Pole pole) {
        this.label = label;
        this.description = description;
        this.axis = axis;
        this.pole = pole;
    }

    public static CoreDriveType from(Octant octant) {
        return switch (octant) {
            case GUARDIAN_LEADER   -> STABILITY_AND_CONTROL;
            case PROPHET_IDEOLOGUE  -> ABSTRACT_MEANING;
            case CAREGIVER_EVERYMAN -> BELONGING_AND_CONNECTION;
            case LEADER_REFORMER    -> EXPRESSION_AND_CREATION;
            case SAGE_ANALYST       -> INDEPENDENCE_AND_TRUTH;
            case SEEKER_INNOVATOR   -> CHANGE_AND_RISK;
            case MASTER_PRAGMATIST  -> MATERIAL_PRAGMATISM;
            case REBEL_PIONEER      -> POWER_AND_DOMINANCE;
        };
    }

    public String label() {
        return label;
    }

    public String description() {
        return description;
    }

    public Axis axis() {
        return axis;
    }

    public Pole pole() {
        return pole;
    }

    public static CoreDriveType of(Axis axis, Pole pole) {
        for (CoreDriveType drive : values()) {
            if (drive.axis == axis && drive.pole == pole) {
                return drive;
            }
        }
        throw new IllegalArgumentException("No core drive for " + axis + " / " + pole);
    }
}
