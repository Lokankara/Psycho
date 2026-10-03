package com.unconscious.collective.quiz.domain.value;

/**
 * The two poles of a coordinate axis. {@code NEGATIVE} maps to the &minus; sign,
 * {@code POSITIVE} to the &plus; sign of the corresponding coordinate.
 */
public enum Pole {

    NEGATIVE(-1),
    POSITIVE(1);

    private final int sign;

    Pole(int sign) {
        this.sign = sign;
    }

    public int sign() {
        return sign;
    }

    /**
     * Resolves a coordinate value in {@code [-1, 1]} to its closest pole.
     * A non-negative value (including exact zero) resolves to {@link #POSITIVE}.
     */
    public static Pole ofSign(double value) {
        return value >= 0 ? POSITIVE : NEGATIVE;
    }
}
