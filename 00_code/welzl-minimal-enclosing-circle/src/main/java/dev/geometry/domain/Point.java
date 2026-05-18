package dev.emil.geometry.domain;

import java.util.Objects;

/**
 * Immutable two-dimensional point.
 */
public record Point(double x, double y) {

    public Point {
        requireFinite(x, "x");
        requireFinite(y, "y");
    }

    public double distanceTo(Point other) {
        return Math.sqrt(distanceSquaredTo(other));
    }

    public double distanceSquaredTo(Point other) {
        Objects.requireNonNull(other, "other must not be null");
        double dx = x - other.x;
        double dy = y - other.y;
        return dx * dx + dy * dy;
    }

    public static double cross(Point origin, Point a, Point b) {
        Objects.requireNonNull(origin, "origin must not be null");
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        return (a.x - origin.x) * (b.y - origin.y) - (a.y - origin.y) * (b.x - origin.x);
    }

    private static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite but was " + value);
        }
    }
}
