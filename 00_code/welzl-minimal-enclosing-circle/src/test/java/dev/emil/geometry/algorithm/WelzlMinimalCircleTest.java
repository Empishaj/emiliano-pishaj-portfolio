package dev.emil.geometry.algorithm;

import dev.emil.geometry.domain.Circle;
import dev.emil.geometry.domain.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WelzlMinimalCircleTest {

    private static final double EPS = 1e-9;

    @Test
    void computesCircleForSinglePoint() {
        List<Point> points = List.of(new Point(3.0, -2.0));

        Circle circle = WelzlMinimalCircle.compute(points, 42L);

        assertEquals(3.0, circle.center().x(), EPS);
        assertEquals(-2.0, circle.center().y(), EPS);
        assertEquals(0.0, circle.radius(), EPS);
        assertTrue(circle.containsAll(points));
    }

    @Test
    void computesCircleForTwoPoints() {
        List<Point> points = List.of(
                new Point(0.0, 0.0),
                new Point(4.0, 0.0)
        );

        Circle circle = WelzlMinimalCircle.compute(points, 42L);

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(0.0, circle.center().y(), EPS);
        assertEquals(2.0, circle.radius(), EPS);
        assertTrue(circle.containsAll(points));
    }

    @Test
    void computesCircleForSquare() {
        List<Point> points = List.of(
                new Point(0.0, 0.0),
                new Point(0.0, 4.0),
                new Point(4.0, 0.0),
                new Point(4.0, 4.0),
                new Point(2.0, 2.0)
        );

        Circle circle = WelzlMinimalCircle.compute(points, 42L);

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(2.0, circle.center().y(), EPS);
        assertEquals(Math.sqrt(8.0), circle.radius(), EPS);
        assertTrue(circle.containsAll(points));
    }

    @Test
    void computesCircleForCollinearPoints() {
        List<Point> points = List.of(
                new Point(-3.0, 0.0),
                new Point(0.0, 0.0),
                new Point(2.0, 0.0),
                new Point(7.0, 0.0)
        );

        Circle circle = WelzlMinimalCircle.compute(points, 42L);

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(0.0, circle.center().y(), EPS);
        assertEquals(5.0, circle.radius(), EPS);
        assertTrue(circle.containsAll(points));
    }

    @Test
    void containsAllInputPointsForMixedPointCloud() {
        List<Point> points = List.of(
                new Point(1.0, 1.0),
                new Point(2.0, 5.0),
                new Point(5.0, 3.0),
                new Point(8.0, 2.0),
                new Point(4.0, 7.0),
                new Point(9.0, 6.0),
                new Point(3.0, 4.0),
                new Point(6.0, 1.0)
        );

        Circle circle = WelzlMinimalCircle.compute(points, 42L);

        assertTrue(circle.containsAll(points));
    }
}
