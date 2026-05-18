# Welzl Minimal Enclosing Circle — Java 21

A console-first Java 21 implementation of **Welzl's randomized algorithm** for computing the minimal enclosing circle of a set of 2D points.

The application reads coordinates from a file and prints the circle center, radius and diameter. It can also generate an SVG visualization of the result.

## What this project demonstrates

- Java 21 records for immutable geometric value objects
- Clean package structure: CLI, algorithm, domain, I/O and rendering
- File-based coordinate input
- Robust handling of single-point, two-point, triangular and collinear cases
- Deterministic randomization via seed
- Maven-based build
- GitHub Actions CI workflow
- JUnit tests for geometry, parsing and algorithmic correctness

## Mathematical problem

Given a finite set of points in the plane, compute the smallest circle that contains all points.

The minimal circle is determined by at most three boundary points:

- one point in the degenerate case,
- two points as a diameter circle,
- three points as a circumcircle.

Welzl's algorithm uses randomized incremental construction. The expected runtime is linear after shuffling the input points.

## Project structure

```text
welzl-minimal-enclosing-circle-java21/
├── pom.xml
├── examples/
│   ├── square.csv
│   ├── triangle.csv
│   ├── collinear.csv
│   └── random-points.csv
├── docs/
│   └── math/
│       └── welzl-algorithm.md
├── src/
│   ├── main/java/dev/emil/geometry/
│   │   ├── cli/
│   │   ├── algorithm/
│   │   ├── domain/
│   │   ├── io/
│   │   └── render/
│   └── test/java/dev/emil/geometry/
└── .github/workflows/maven.yml
```

## Input file format

The parser accepts one point per line. Blank lines and comments starting with `#` are ignored.

Supported formats:

```text
x,y
x;y
x y
```

Example:

```csv
# x,y
0,0
0,4
4,0
4,4
2,2
```

## Build

```bash
mvn clean verify
```

## Run with Maven

```bash
mvn exec:java -Dexec.args="examples/square.csv --seed 42"
```

Expected output:

```text
Minimal Enclosing Circle
----------------------------------------
Input file:   examples/square.csv
Points:       5
Seed:         42
Center X:     2.0000000000
Center Y:     2.0000000000
Radius:       2.8284271247
Diameter:     5.6568542495
```

## Generate SVG output

```bash
mvn exec:java -Dexec.args="examples/random-points.csv --seed 42 --svg target/result.svg"
```

The SVG contains all input points, the computed minimal enclosing circle and the circle center.

## Run as JAR

```bash
mvn clean package
java -jar target/welzl-minimal-enclosing-circle-java21-1.0.0-SNAPSHOT.jar examples/square.csv --seed 42
```

## Test cases included

The project includes tests for:

- one point,
- two points,
- square corner points,
- collinear points,
- mixed point cloud containment,
- point file parsing,
- invalid input rejection,
- circumcircle calculation.

## Why this project is useful for a GitHub portfolio

This project is small enough to understand quickly, but mathematically and technically strong enough to demonstrate algorithmic thinking, clean Java 21 modeling, file I/O, deterministic execution, Maven build discipline and CI readiness.
