package dev.emil.geometry.cli;

import dev.emil.geometry.algorithm.WelzlMinimalCircle;
import dev.emil.geometry.domain.Circle;
import dev.emil.geometry.domain.Point;
import dev.emil.geometry.io.PointFileReader;
import dev.emil.geometry.render.SvgCircleRenderer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public final class MinimalCircleCli {

    private MinimalCircleCli() {
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        try {
            CliOptions options = CliOptions.parse(args);
            List<Point> points = PointFileReader.read(options.inputFile());

            if (points.isEmpty()) {
                throw new IllegalArgumentException("Input file contains no valid points.");
            }

            Circle circle = WelzlMinimalCircle.compute(points, options.seed());

            printResult(points, circle, options.seed(), options.inputFile());

            if (options.svgOutputFile() != null) {
                SvgCircleRenderer.write(options.svgOutputFile(), points, circle);
                System.out.println();
                System.out.println("SVG written to: " + options.svgOutputFile());
            }
        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
            System.err.println();
            printUsage();
            System.exit(1);
        }
    }

    private static void printResult(List<Point> points, Circle circle, long seed, Path inputFile) {
        System.out.println("Minimal Enclosing Circle");
        System.out.println("----------------------------------------");
        System.out.printf("Input file:   %s%n", inputFile);
        System.out.printf("Points:       %d%n", points.size());
        System.out.printf("Seed:         %d%n", seed);
        System.out.printf("Center X:     %.10f%n", circle.center().x());
        System.out.printf("Center Y:     %.10f%n", circle.center().y());
        System.out.printf("Radius:       %.10f%n", circle.radius());
        System.out.printf("Diameter:     %.10f%n", circle.radius() * 2.0);
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  mvn exec:java -Dexec.args=\"<points-file> [--seed <number>] [--svg <output-file>]\"");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  mvn exec:java -Dexec.args=\"examples/square.csv --seed 42\"");
        System.out.println("  mvn exec:java -Dexec.args=\"examples/random-points.csv --seed 42 --svg target/result.svg\"");
        System.out.println();
        System.out.println("Supported input formats per line:");
        System.out.println("  x,y");
        System.out.println("  x;y");
        System.out.println("  x y");
    }

    private record CliOptions(Path inputFile, long seed, Path svgOutputFile) {

        static CliOptions parse(String[] args) {
            if (args.length == 0) {
                throw new IllegalArgumentException("Missing input file.");
            }

            Path inputFile = null;
            Long seed = null;
            Path svgOutputFile = null;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];

                switch (arg) {
                    case "--seed" -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Missing value after --seed.");
                        }
                        seed = Long.parseLong(args[++i]);
                    }
                    case "--svg" -> {
                        if (i + 1 >= args.length) {
                            throw new IllegalArgumentException("Missing value after --svg.");
                        }
                        svgOutputFile = Path.of(args[++i]);
                    }
                    default -> {
                        if (arg.startsWith("--")) {
                            throw new IllegalArgumentException("Unknown option: " + arg);
                        }
                        if (inputFile != null) {
                            throw new IllegalArgumentException("Only one input file is supported.");
                        }
                        inputFile = Path.of(arg);
                    }
                }
            }

            if (inputFile == null) {
                throw new IllegalArgumentException("Missing input file.");
            }

            long resolvedSeed = seed != null ? seed : Instant.now().toEpochMilli();
            return new CliOptions(inputFile, resolvedSeed, svgOutputFile);
        }
    }
}
