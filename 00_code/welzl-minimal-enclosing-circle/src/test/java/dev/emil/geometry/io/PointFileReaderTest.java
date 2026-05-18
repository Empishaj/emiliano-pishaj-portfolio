package dev.emil.geometry.io;

import dev.emil.geometry.domain.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PointFileReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void readsCommaSemicolonAndWhitespaceSeparatedPoints() throws IOException {
        Path file = tempDir.resolve("points.txt");
        Files.writeString(file, """
                # comment
                1,2
                3;4
                5 6
                """);

        List<Point> points = PointFileReader.read(file);

        assertEquals(List.of(
                new Point(1.0, 2.0),
                new Point(3.0, 4.0),
                new Point(5.0, 6.0)
        ), points);
    }

    @Test
    void rejectsInvalidLines() throws IOException {
        Path file = tempDir.resolve("invalid.txt");
        Files.writeString(file, "1,2,3");

        assertThrows(IllegalArgumentException.class, () -> PointFileReader.read(file));
    }
}
