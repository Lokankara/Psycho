package com.unconscious.collective.quiz.domain.value;

import java.io.Serializable;

/**
 * An immutable point in the continuous 3D semantic space. Every component is
 * clamped into the strict {@code [-1.0, 1.0]} range.
 */
public record Coordinates(double x, double y, double z) implements Serializable {

    /** Creates coordinates clamped to {@code [-1, 1]}, preserving NaN components. */
    public static Coordinates of(double x, double y, double z) {
        return new Coordinates(clamp(x), clamp(y), clamp(z));
    }

    private static double clamp(double value) {
        return Math.clamp(value, -1.0, 1.0);
    }

    public double value(Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    /** Returns the axis pole, treating zero as positive and NaN as negative. */
    public Pole pole(Axis axis) {
        return Pole.ofSign(value(axis));
    }
}
