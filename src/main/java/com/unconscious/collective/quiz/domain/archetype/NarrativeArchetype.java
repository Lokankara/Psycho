package com.unconscious.collective.quiz.domain.archetype;

import java.util.Arrays;

public enum NarrativeArchetype implements Archetype {

    JOURNEY("Путь / Путешествие", CoreDriveType.INDEPENDENCE_AND_TRUTH,
            new Shadow("Блуждание без цели")),
    DEATH_REBIRTH("Смерть и Возрождение", CoreDriveType.CHANGE_AND_RISK,
            new Shadow("Зацикленность на разрушении")),
    ORDER_VS_CHAOS("Борьба Порядка и Хаоса", CoreDriveType.STABILITY_AND_CONTROL,
            new Shadow("Тоталитарный контроль")),
    FALL("Падение", CoreDriveType.SURVIVAL_AND_SAFETY,
            new Shadow("Саморазрушение, утрата смысла")),
    THE_QUEST("Поиски / Заветная цель", CoreDriveType.ABSTRACT_MEANING,
            new Shadow("Ослепление целью, уничтожение окружающих ради результата")),
    INITIATION("Инициация / Взросление", CoreDriveType.EXPRESSION_AND_CREATION,
            new Shadow("Отказ от взросления, застревание в травме")),
    OVERCOMING_THE_MONSTER("Победа над чудовищем", CoreDriveType.POWER_AND_DOMINANCE,
            new Shadow("Превращение в новое чудовище в процессе борьбы")),
    COMEDY_TRICKERY("Комедия / Плутовство", CoreDriveType.PLAY_AND_HEDONISM,
            new Shadow("Полный хаос, обесценивание смыслов и аморализм")),
    SACRIFICE("Жертвоприношение", CoreDriveType.BELONGING_AND_CONNECTION,
            new Shadow("Напрасная жертва, токсичный комплекс мученика")),
    RETURN("Возвращение домой", CoreDriveType.MATERIAL_PRAGMATISM,
            new Shadow("Неспособность примениться к мирной жизни, ностальгическая стагнация"));

    private final String title;
    private final CoreDriveType driver;
    private final Shadow shadow;

    NarrativeArchetype(String title, CoreDriveType driver, Shadow shadow) {
        this.title = title;
        this.driver = driver;
        this.shadow = shadow;
    }

    public static NarrativeArchetype from(CoreDriveType coreDrive) {
        return Arrays.stream(values()).filter(archetype -> archetype.getDriver() == coreDrive).findFirst().orElse(null);
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
