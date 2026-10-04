package com.unconscious.collective.quiz.domain.quiz.symbol;

public enum SymbolCategoryType {

    PERSONA("Персона"),
    SHADOW("Тень"),
    ANIMA_ANIMUS("Анима и Анимус"),
    SELF("Самость"),
    WORLD_TREE("Мировое древо"),
    GREAT_MOTHER("Великая мать"),
    HERO("Герой-освободитель"),
    WISE_OLD_MAN("Мудрый старик"),
    TRICKSTER("Трикстер"),
    THRESHOLD_GUARDIAN("Страж порога"),
    DEVOURING_FATHER("Пожирающий отец"),
    ABYSS_OCEAN("Бездна / Океан"),
    TRANSFORMATION("Трансформация"),
    CHILD("Дитя / Уроборос"),
    DIVINE_COUPLE("Сизигия / Священный брак"),
    SACRIFICE("Жертвоприношение"),
    NIGHT_SEA_JOURNEY("Ночное плавание"),
    LABYRINTH("Лабиринт / Путь"),
    AXIS_MUNDI("Ось мира"),
    TOWER("Башня / Изоляция"),
    TREASURE("Сокровище / Грааль"),
    REBIRTH("Возрождение");

    private final String title;

    SymbolCategoryType(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
