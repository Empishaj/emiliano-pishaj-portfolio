package dev.emil.geometry.io;

import dev.emil.geometry.domain.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Reads 2D points from a text file.
 *
 * <p>Supported line formats:</p>
 * <pre>
 * x,y
 * x;y
 * x y
 * </pre>
 *
 * <p>Blank lines and lines starting with {@code #} are ignored.</p>
 */
public final class PointFileReader {

    private PointFileReader() {
    }

    public static List<Point> read(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");

        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Input file does not exist: " + path);
        }

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException("Input path is not a regular file: " + path);
        }

        List<Point> points = new ArrayList<>();
        List<String> lines = Files.readAllLines(path);

        for (int index = 0; index < lines.size(); index++) {
            int lineNumber = index + 1;
            String rawLine = lines.get(index);
            String line = rawLine.trim();

            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }

            points.add(parsePoint(line, lineNumber));
        }

        return List.copyOf(points);
    }

    private static Point parsePoint(String line, int lineNumber) {
        String normalized = line
                .replace(';', ' ')
                .replace(',', ' ')
                .trim();

        String[] parts = normalized.split("\\s+");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid point format in line " + lineNumber + ": '" + line + "'. "
                            + "Expected exactly two numeric values."
            );
        }

        try {
            double x = Double.parseDouble(parts[0]);
            double y = Double.parseDouble(parts[1]);
            return new Point(x, y);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid numeric value in line " + lineNumber + ": '" + line + "'.",
                    exception
            );
        }
    }
}
