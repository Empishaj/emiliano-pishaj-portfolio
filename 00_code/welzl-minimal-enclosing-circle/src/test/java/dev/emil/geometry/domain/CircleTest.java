package dev.emil.geometry.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircleTest {

    private static final double EPS = 1e-9;

    @Test
    void createsCircleFromDiameter() {
        Circle circle = Circle.fromDiameter(new Point(0.0, 0.0), new Point(4.0, 0.0));

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(0.0, circle.center().y(), EPS);
        assertEquals(2.0, circle.radius(), EPS);
    }

    @Test
    void createsCircumcircleFromThreePoints() {
        Circle circle = Circle.fromThreeBoundaryPoints(
                new Point(0.0, 0.0),
                new Point(0.0, 4.0),
                new Point(4.0, 0.0)
        ).orElseThrow();

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(2.0, circle.center().y(), EPS);
        assertEquals(Math.sqrt(8.0), circle.radius(), EPS);
    }

    @Test
    void handlesCollinearBoundaryPointsByUsingDiameterCircle() {
        Circle circle = Circle.smallestCircleFromBoundaryPoints(List.of(
                new Point(-3.0, 0.0),
                new Point(0.0, 0.0),
                new Point(7.0, 0.0)
        ));

        assertEquals(2.0, circle.center().x(), EPS);
        assertEquals(0.0, circle.center().y(), EPS);
        assertEquals(5.0, circle.radius(), EPS);
        assertTrue(circle.contains(new Point(0.0, 0.0)));
    }
}
