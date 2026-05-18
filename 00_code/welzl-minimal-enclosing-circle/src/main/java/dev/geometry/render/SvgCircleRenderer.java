package dev.emil.geometry.render;

import dev.emil.geometry.domain.Circle;
import dev.emil.geometry.domain.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Writes a small SVG visualization of the point cloud and the computed circle.
 */
public final class SvgCircleRenderer {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;
    private static final int PADDING = 50;

    private SvgCircleRenderer() {
    }

    public static void write(Path outputFile, List<Point> points, Circle circle) throws IOException {
        Objects.requireNonNull(outputFile, "outputFile must not be null");
        Objects.requireNonNull(points, "points must not be null");
        Objects.requireNonNull(circle, "circle must not be null");

        Bounds bounds = Bounds.from(points, circle);
        StringBuilder svg = new StringBuilder();
        svg.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        svg.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"").append(WIDTH).append("\" height=\"").append(HEIGHT).append("\" viewBox=\"0 0 ").append(WIDTH).append(' ').append(HEIGHT).append("\">\n");
        svg.append("  <rect width=\"100%\" height=\"100%\" fill=\"white\"/>\n");
        svg.append("  <text x=\"20\" y=\"30\" font-family=\"Arial\" font-size=\"18\">Minimal Enclosing Circle</text>\n");

        double svgCenterX = bounds.toSvgX(circle.center().x());
        double svgCenterY = bounds.toSvgY(circle.center().y());
        double svgRadius = circle.radius() * bounds.scale();

        svg.append(String.format(Locale.US,
                "  <circle cx=\"%.6f\" cy=\"%.6f\" r=\"%.6f\" fill=\"none\" stroke=\"#2563eb\" stroke-width=\"3\"/>%n",
                svgCenterX,
                svgCenterY,
                svgRadius));

        svg.append(String.format(Locale.US,
                "  <circle cx=\"%.6f\" cy=\"%.6f\" r=\"5\" fill=\"#dc2626\"/>%n",
                svgCenterX,
                svgCenterY));

        for (Point point : points) {
            svg.append(String.format(Locale.US,
                    "  <circle cx=\"%.6f\" cy=\"%.6f\" r=\"4\" fill=\"#111827\"/>%n",
                    bounds.toSvgX(point.x()),
                    bounds.toSvgY(point.y())));
        }

        svg.append(String.format(Locale.US,
                "  <text x=\"20\" y=\"%d\" font-family=\"Arial\" font-size=\"14\">center=(%.6f, %.6f), radius=%.6f, points=%d</text>%n",
                HEIGHT - 20,
                circle.center().x(),
                circle.center().y(),
                circle.radius(),
                points.size()));
        svg.append("</svg>\n");

        Path parent = outputFile.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(outputFile, svg.toString());
    }

    private record Bounds(double minX, double maxX, double minY, double maxY, double scale) {

        static Bounds from(List<Point> points, Circle circle) {
            double circleMinX = circle.center().x() - circle.radius();
            double circleMaxX = circle.center().x() + circle.radius();
            double circleMinY = circle.center().y() - circle.radius();
            double circleMaxY = circle.center().y() + circle.radius();

            double minPointX = points.stream().min(Comparator.comparingDouble(Point::x)).map(Point::x).orElse(circleMinX);
            double maxPointX = points.stream().max(Comparator.comparingDouble(Point::x)).map(Point::x).orElse(circleMaxX);
            double minPointY = points.stream().min(Comparator.comparingDouble(Point::y)).map(Point::y).orElse(circleMinY);
            double maxPointY = points.stream().max(Comparator.comparingDouble(Point::y)).map(Point::y).orElse(circleMaxY);

            double minX = Math.min(minPointX, circleMinX);
            double maxX = Math.max(maxPointX, circleMaxX);
            double minY = Math.min(minPointY, circleMinY);
            double maxY = Math.max(maxPointY, circleMaxY);

            double rangeX = Math.max(1e-9, maxX - minX);
            double rangeY = Math.max(1e-9, maxY - minY);
            double scale = Math.min((WIDTH - 2.0 * PADDING) / rangeX, (HEIGHT - 2.0 * PADDING) / rangeY);

            return new Bounds(minX, maxX, minY, maxY, scale);
        }

        double toSvgX(double x) {
            return PADDING + (x - minX) * scale;
        }

        double toSvgY(double y) {
            return HEIGHT - PADDING - (y - minY) * scale;
        }
    }
}
