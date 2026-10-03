package com.unconscious.collective.quiz.domain.value;

public enum Sign {

    NEGATIVE(-1),
    POSITIVE(1);

    private final int value;

    Sign(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public static Sign of(double coordinate) {
        return coordinate > 0 ? POSITIVE : NEGATIVE;
    }
}
