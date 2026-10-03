package com.unconscious.collective.quiz.domain.value;

public record Vector3D(double x, double y, double z) {

    private static final double MIN = -1.0;
    private static final double MAX = 1.0;

    public Vector3D {
        x = Math.clamp(x, MIN, MAX);
        y = Math.clamp(y, MIN, MAX);
        z = Math.clamp(z, MIN, MAX);
    }

    public double value(Axis axis) {
        return switch (axis) {
            case X -> x;
            case Y -> y;
            case Z -> z;
        };
    }

    public Sign sign(Axis axis) {
        return Sign.of(value(axis));
    }

    public double distanceTo(Vector3D other) {
        return Math.sqrt(
                Math.pow(x - other.x, 2)
                        + Math.pow(y - other.y, 2)
                        + Math.pow(z - other.z, 2));
    }
}
