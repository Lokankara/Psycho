package com.unconscious.collective.quiz.domain.archetype;

public enum PersonaArchetype implements Archetype {

    SAGE("Мудрец", CoreDriveType.INDEPENDENCE_AND_TRUTH,
            new Shadow("Догматик, оторванный от реальности теоретик")),
    SEEKER("Искатель", CoreDriveType.INDEPENDENCE_AND_TRUTH,
            new Shadow("Изоляция, эгоцентризм, вечный побег")),
    INNOCENT("Простодушный", CoreDriveType.SURVIVAL_AND_SAFETY,
            new Shadow("Наивность, отрицание реальных проблем")),

    RULER("Правитель", CoreDriveType.POWER_AND_DOMINANCE,
            new Shadow("Тиран, бюрократ, контролер-параноик")),
    CREATOR("Творец", CoreDriveType.EXPRESSION_AND_CREATION,
            new Shadow("Перфекционист, созидание ради процесса")),
    CAREGIVER("Опекун", CoreDriveType.STABILITY_AND_CONTROL,
            new Shadow("Потеря индивидуальности, гиперопека, манипуляция виной")),

    HERO("Герой", CoreDriveType.CHANGE_AND_RISK,
            new Shadow("Агрессор, гордец, не умеющий отступить")),
    REBEL("Бунтарь", CoreDriveType.CHANGE_AND_RISK,
            new Shadow("Бессмысленное разрушение, авантюризм")),
    MAGICIAN("Маг", CoreDriveType.ABSTRACT_MEANING,
            new Shadow("Манипуляция, использование людей в своих целях")),

    JESTER("Шут", CoreDriveType.PLAY_AND_HEDONISM,
            new Shadow("Цинизм, отсутствие долгосрочного видения")),
    EVERYMAN("Славный малый", CoreDriveType.BELONGING_AND_CONNECTION,
            new Shadow("Конформизм, слепая покорность")),
    LOVER("Любовник", CoreDriveType.MATERIAL_PRAGMATISM,
            new Shadow("Зависимость, фиксация на гедонизме и объекте"));

    private final String title;
    private final CoreDriveType drive;
    private final Shadow shadow;

    PersonaArchetype(String title, CoreDriveType drive, Shadow shadow) {
        this.title = title;
        this.drive = drive;
        this.shadow = shadow;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Shadow getShadow() {
        return shadow;
    }

    @Override
    public CoreDriveType getDriver() {
        return drive;
    }

    public CoreDriveType drive() {
        return drive;
    }
}
