package dev.emil.geometry.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable circle represented by center and radius.
 */
public record Circle(Point center, double radius) {

    public static final double EPSILON = 1e-10;

    public Circle {
        Objects.requireNonNull(center, "center must not be null");
        if (!Double.isFinite(radius)) {
            throw new IllegalArgumentException("radius must be finite but was " + radius);
        }
        if (radius < -EPSILON) {
            throw new IllegalArgumentException("radius must not be negative but was " + radius);
        }
        radius = Math.max(0.0, radius);
    }

    public boolean contains(Point point) {
        Objects.requireNonNull(point, "point must not be null");
        return center.distanceSquaredTo(point) <= radius * radius + EPSILON;
    }

    public boolean containsAll(List<Point> points) {
        Objects.requireNonNull(points, "points must not be null");
        return points.stream().allMatch(this::contains);
    }

    public static Circle fromSinglePoint(Point point) {
        Objects.requireNonNull(point, "point must not be null");
        return new Circle(point, 0.0);
    }

    public static Circle fromDiameter(Point a, Point b) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        Point center = new Point((a.x() + b.x()) / 2.0, (a.y() + b.y()) / 2.0);
        return new Circle(center, center.distanceTo(a));
    }

    /**
     * Computes the circumcircle for three non-collinear points.
     *
     * @return empty if the three points are collinear or nearly collinear
     */
    public static Optional<Circle> fromThreeBoundaryPoints(Point a, Point b, Point c) {
        Objects.requireNonNull(a, "a must not be null");
        Objects.requireNonNull(b, "b must not be null");
        Objects.requireNonNull(c, "c must not be null");

        double ax = a.x();
        double ay = a.y();
        double bx = b.x();
        double by = b.y();
        double cx = c.x();
        double cy = c.y();

        double determinant = 2.0 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));

        if (Math.abs(determinant) <= EPSILON) {
            return Optional.empty();
        }

        double aSquared = ax * ax + ay * ay;
        double bSquared = bx * bx + by * by;
        double cSquared = cx * cx + cy * cy;

        double centerX = (aSquared * (by - cy) + bSquared * (cy - ay) + cSquared * (ay - by)) / determinant;
        double centerY = (aSquared * (cx - bx) + bSquared * (ax - cx) + cSquared * (bx - ax)) / determinant;

        Point center = new Point(centerX, centerY);
        return Optional.of(new Circle(center, center.distanceTo(a)));
    }

    /**
     * Computes the smallest circle defined by up to three boundary candidates.
     * This helper is useful for tests and for explaining degenerate cases.
     */
    public static Circle smallestCircleFromBoundaryPoints(List<Point> boundaryPoints) {
        Objects.requireNonNull(boundaryPoints, "boundaryPoints must not be null");
        return switch (boundaryPoints.size()) {
            case 0 -> new Circle(new Point(0.0, 0.0), 0.0);
            case 1 -> fromSinglePoint(boundaryPoints.getFirst());
            case 2 -> fromDiameter(boundaryPoints.get(0), boundaryPoints.get(1));
            case 3 -> fromThreeBoundaryPoints(boundaryPoints.get(0), boundaryPoints.get(1), boundaryPoints.get(2))
                    .orElseGet(() -> smallestDiameterCircle(boundaryPoints));
            default -> throw new IllegalArgumentException("boundaryPoints must contain at most three points");
        };
    }

    private static Circle smallestDiameterCircle(List<Point> points) {
        return List.of(
                        fromDiameter(points.get(0), points.get(1)),
                        fromDiameter(points.get(0), points.get(2)),
                        fromDiameter(points.get(1), points.get(2))
                ).stream()
                .filter(circle -> circle.containsAll(points))
                .min(Comparator.comparingDouble(Circle::radius))
                .orElseThrow(() -> new IllegalStateException("No diameter circle contains all boundary points."));
    }
}
