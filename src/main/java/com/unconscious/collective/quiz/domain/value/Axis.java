package com.unconscious.collective.quiz.domain.value;

public enum Axis {

    X("Социальный вектор", "Индивидуализм", "Коллективизм"),
    Y("Вектор изменений", "Стабильность", "Трансформация"),
    Z("Понятийный вектор", "Материализм", "Абстракция");

    private final String title;
    private final String negativePole;
    private final String positivePole;

    Axis(String title, String negativePole, String positivePole) {
        this.title = title;
        this.negativePole = negativePole;
        this.positivePole = positivePole;
    }

    public String title() {
        return title;
    }

    public String negativePole() {
        return negativePole;
    }

    public String positivePole() {
        return positivePole;
    }
}
