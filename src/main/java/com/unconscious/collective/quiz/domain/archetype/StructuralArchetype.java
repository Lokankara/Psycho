package com.unconscious.collective.quiz.domain.archetype;

import lombok.Getter;

public enum StructuralArchetype implements Archetype {
    EGO("Эго (Ego)", CoreDriveType.POWER_AND_DOMINANCE,
            "Центр сознания, отвечающий за личностную идентичность",
            new Shadow("Гипертрофированный эгоизм, разрыв связи с бессознательным")),
    PERSONA("Персона (Persona)", CoreDriveType.BELONGING_AND_CONNECTION,
            "Социальная маска, адаптирующая человека к обществу",
            new Shadow("Лицемерие, конформизм, полная утрата истинного Я")),
    SHADOW("Тень (Shadow)", CoreDriveType.SURVIVAL_AND_SAFETY,
            "Подавленные, бессознательные и инстинктивные аспекты личности",
            new Shadow("Аморальность, слепая деструктивность, проецирование своих пороков на других")),
    ANIMA("Анима (Anima)", CoreDriveType.EXPRESSION_AND_CREATION,
            "Бессознательное женское начало в психике мужчины",
            new Shadow("Иррациональные капризы, депрессивность, эмоциональная неуправляемость")),
    ANIMUS("Анимус (Animus)", CoreDriveType.ABSTRACT_MEANING,
            "Бессознательное мужское начало в психике женщины",
            new Shadow("Догматизм, жесткая судимость, холодный авторитаризм")),
    SELF("Самость (Self)", CoreDriveType.STABILITY_AND_CONTROL,
            "Архетип цельности, объединяющий сознание и бессознательное",
            new Shadow("Мания величия, иллюзия божественности, инфляция Эго"));

    private final String title;
    private final CoreDriveType driver;
    @Getter
    private final String functionDescription;
    private final Shadow shadow;

    StructuralArchetype(String title, CoreDriveType driver, String functionDescription, Shadow shadow) {
        this.title = title;
        this.driver = driver;
        this.functionDescription = functionDescription;
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
        return driver;
    }
}