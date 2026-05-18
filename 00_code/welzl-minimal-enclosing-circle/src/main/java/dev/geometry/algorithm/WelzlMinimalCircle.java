package dev.geometry.algorithm;

import dev.emil.geometry.domain.Circle;
import dev.emil.geometry.domain.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Computes the minimal enclosing circle for a set of 2D points.
 *
 * <p>
 * This is an iterative randomized variant of Welzl's algorithm. The expected
 * runtime is linear for randomly shuffled input.
 * </p>
 */
public final class WelzlMinimalCircle {

	private WelzlMinimalCircle() {
	}

	public static Circle compute(List<Point> inputPoints, long seed) {
		Objects.requireNonNull(inputPoints, "inputPoints must not be null");
		
		if (inputPoints.isEmpty()) {
			throw new IllegalArgumentException("At least one point is required.");
		}

		List<Point> points = new ArrayList<>(inputPoints);
	
		Collections.shuffle(points, new Random(seed));

		Circle circle = null;

		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (circle == null || !circle.contains(p)) {
				circle = circleWithOneBoundaryPoint(points.subList(0, i + 1), p);
			}
		}

		if (circle == null || !circle.containsAll(inputPoints)) {
			throw new IllegalStateException("Algorithm failed to compute an enclosing circle.");
		}

		return circle;
	}

	private static Circle circleWithOneBoundaryPoint(List<Point> points, Point boundaryPoint) {
		Circle circle = Circle.fromSinglePoint(boundaryPoint);

		for (int i = 0; i < points.size(); i++) {
			Point q = points.get(i);
			if (!circle.contains(q)) {
				if (circle.radius() == 0.0) {
					circle = Circle.fromDiameter(boundaryPoint, q);
				} else {
					circle = circleWithTwoBoundaryPoints(points.subList(0, i + 1), boundaryPoint, q);
				}
			}
		}

		return circle;
	}

	/**
	 * Computes a minimal circle containing {@code points} with
	 * {@code firstBoundaryPoint} and {@code secondBoundaryPoint} on the boundary
	 * whenever such a circle is required.
	 *
	 * <p>
	 * The left/right candidate selection follows the robust iterative form commonly
	 * used for randomized smallest enclosing circle implementations.
	 * </p>
	 */
	private static Circle circleWithTwoBoundaryPoints(List<Point> points, Point firstBoundaryPoint,
			Point secondBoundaryPoint) {
		Circle diameterCircle = Circle.fromDiameter(firstBoundaryPoint, secondBoundaryPoint);
		Circle leftCandidate = null;
		Circle rightCandidate = null;

		for (Point r : points) {
			if (diameterCircle.contains(r)) {
				continue;
			}

			Optional<Circle> maybeCircumcircle = Circle.fromThreeBoundaryPoints(firstBoundaryPoint, secondBoundaryPoint,
					r);

			if (maybeCircumcircle.isEmpty()) {
				continue;
			}

			Circle circumcircle = maybeCircumcircle.get();
			double cross = Point.cross(firstBoundaryPoint, secondBoundaryPoint, r);
			double centerCross = Point.cross(firstBoundaryPoint, secondBoundaryPoint, circumcircle.center());

			if (cross > 0.0) {
				if (leftCandidate == null
						|| centerCross > Point.cross(firstBoundaryPoint, secondBoundaryPoint, leftCandidate.center())) {
					leftCandidate = circumcircle;
				}
			} else if (cross < 0.0) {
				if (rightCandidate == null || centerCross < Point.cross(firstBoundaryPoint, secondBoundaryPoint,
						rightCandidate.center())) {
					rightCandidate = circumcircle;
				}
			}
		}

		if (leftCandidate == null && rightCandidate == null) {
			return diameterCircle;
		}

		if (leftCandidate == null) {
			return rightCandidate;
		}

		if (rightCandidate == null) {
			return leftCandidate;
		}

		return leftCandidate.radius() <= rightCandidate.radius() ? leftCandidate : rightCandidate;
	}
}
