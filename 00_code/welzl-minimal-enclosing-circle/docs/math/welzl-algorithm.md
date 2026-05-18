# Welzl's Algorithm — Minimal Enclosing Circle

The minimal enclosing circle of a finite set of two-dimensional points is the smallest circle that contains every point.

A key geometric fact is that the minimal enclosing circle is defined by at most three boundary points:

- one point for the degenerate single-point case,
- two points when the segment between them is the circle diameter,
- three points when the circle is the circumcircle of a triangle.

Welzl's algorithm is a randomized incremental algorithm. The input points are shuffled first. Then the algorithm adds points one by one and recomputes the circle only when the current point lies outside the current circle.

This project uses an iterative randomized implementation with deterministic seeding for reproducible command-line runs.

## Numerical tolerance

Floating-point geometry is sensitive to rounding errors. Therefore, containment checks use a small epsilon value. This prevents points that are mathematically on the boundary from being rejected because of tiny floating-point deviations.
