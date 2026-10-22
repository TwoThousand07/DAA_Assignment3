package edu.aitu.daa.benchmark;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class PlotGenerator {
    private record Row(String algorithm, String graph, int n, int m, double time, long dsuOps,
                       long edgeScans, long comparisons, long maxDepth, long result) {
        String density() {
            return graph.endsWith("dense") ? "dense" : "sparse";
        }

        double operations() {
            return dsuOps + edgeScans + comparisons;
        }
    }

    private record Point(double x, double y) {
    }

    private record Series(String label, List<Point> points, Color color) {
    }

    private static final Color[] COLORS = {
            new Color(31, 119, 180), new Color(255, 127, 14), new Color(44, 160, 44),
            new Color(214, 39, 40), new Color(148, 103, 189), new Color(140, 86, 75),
            new Color(227, 119, 194), new Color(127, 127, 127), new Color(188, 189, 34),
            new Color(23, 190, 207)
    };

    public void generate(Path csv, Path outputDirectory) throws IOException {
        System.setProperty("java.awt.headless", "true");
        Files.createDirectories(outputDirectory);
        List<Row> rows = read(csv);
        operationsPlot(rows, outputDirectory.resolve("operations_vs_n.png"));
        scansPerEdgePlot(rows, outputDirectory.resolve("scans_per_edge.png"));
        kruskalSharePlot(rows, outputDirectory.resolve("kruskal_edge_share.png"));
    }

    private List<Row> read(Path csv) throws IOException {
        List<Row> rows = new ArrayList<>();
        List<String> lines = Files.readAllLines(csv);
        for (int i = 1; i < lines.size(); i++) {
            String[] part = lines.get(i).split(",", -1);
            if (part.length != 10) {
                continue;
            }
            rows.add(new Row(part[0], part[1], Integer.parseInt(part[2]), Integer.parseInt(part[3]),
                    Double.parseDouble(part[4]), Long.parseLong(part[5]), Long.parseLong(part[6]),
                    Long.parseLong(part[7]), Long.parseLong(part[8]), Long.parseLong(part[9])));
        }
        return rows;
    }

    private void operationsPlot(List<Row> rows, Path output) throws IOException {
        List<Series> series = new ArrayList<>();
        int colorIndex = 0;
        for (String algorithm : List.of("Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath")) {
            for (String density : List.of("sparse", "dense")) {
                List<Point> points = rows.stream()
                        .filter(row -> row.algorithm().equals(algorithm) && row.density().equals(density))
                        .sorted(Comparator.comparingInt(Row::n))
                        .map(row -> new Point(row.n(), Math.max(1, row.operations())))
                        .toList();
                series.add(new Series(algorithm + " " + density, points, COLORS[colorIndex++ % COLORS.length]));
            }
        }
        drawPlot(output, "Operation counters vs n", "Vertices n (log scale)", "dsuOps + edgeScans + comparisons (log scale)",
                series, true, true, 0, 0);
    }

    private void scansPerEdgePlot(List<Row> rows, Path output) throws IOException {
        List<Series> series = new ArrayList<>();
        int colorIndex = 0;
        for (String algorithm : List.of("Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath")) {
            for (String density : List.of("sparse", "dense")) {
                List<Point> points = rows.stream()
                        .filter(row -> row.algorithm().equals(algorithm) && row.density().equals(density))
                        .sorted(Comparator.comparingInt(Row::n))
                        .map(row -> new Point(row.n(), row.m() == 0 ? 0 : (double) row.edgeScans() / row.m()))
                        .toList();
                series.add(new Series(algorithm + " " + density, points, COLORS[colorIndex++ % COLORS.length]));
            }
        }
        drawPlot(output, "Edge scans per input edge", "Vertices n (log scale)", "edgeScans / m", series, true, false, 0, 2.2);
    }

    private void kruskalSharePlot(List<Row> rows, Path output) throws IOException {
        List<Series> series = new ArrayList<>();
        int colorIndex = 0;
        for (String density : List.of("sparse", "dense")) {
            List<Point> points = rows.stream()
                    .filter(row -> row.algorithm().equals("Kruskal") && row.density().equals(density))
                    .sorted(Comparator.comparingInt(Row::n))
                    .map(row -> new Point(row.n(), row.m() == 0 ? 0 : (double) row.edgeScans() / row.m()))
                    .toList();
            series.add(new Series("Kruskal " + density, points, COLORS[colorIndex++ % COLORS.length]));
        }
        drawPlot(output, "Share of edges examined by Kruskal", "Vertices n (log scale)", "edgeScans / m", series, true, false, 0, 1.05);
    }

    private void drawPlot(Path output, String title, String xLabel, String yLabel, List<Series> series,
                          boolean logX, boolean logY, double requestedMinY, double requestedMaxY) throws IOException {
        int width = 1200;
        int height = 800;
        int left = 105;
        int right = 330;
        int top = 80;
        int bottom = 100;
        int plotWidth = width - left - right;
        int plotHeight = height - top - bottom;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Series item : series) {
            for (Point point : item.points()) {
                minX = Math.min(minX, point.x());
                maxX = Math.max(maxX, point.x());
                minY = Math.min(minY, point.y());
                maxY = Math.max(maxY, point.y());
            }
        }
        if (logY) {
            minY = Math.max(1, minY);
            maxY = Math.max(minY * 1.1, maxY);
        } else if (requestedMaxY > requestedMinY) {
            minY = requestedMinY;
            maxY = requestedMaxY;
        } else {
            minY = Math.min(0, minY);
            maxY = Math.max(1, maxY * 1.1);
        }
        double lx0 = logX ? Math.log10(minX) : minX;
        double lx1 = logX ? Math.log10(maxX) : maxX;
        double ly0 = logY ? Math.log10(minY) : minY;
        double ly1 = logY ? Math.log10(maxY) : maxY;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString(title, left, 40);
        g.setFont(new Font("SansSerif", Font.PLAIN, 14));
        for (int i = 0; i <= 5; i++) {
            int x = left + i * plotWidth / 5;
            double value = lx0 + (lx1 - lx0) * i / 5.0;
            double shown = logX ? Math.pow(10, value) : value;
            g.setColor(new Color(225, 225, 225));
            g.drawLine(x, top, x, top + plotHeight);
            g.setColor(Color.BLACK);
            g.drawString(format(shown), x - 18, top + plotHeight + 25);
        }
        for (int i = 0; i <= 5; i++) {
            int y = top + plotHeight - i * plotHeight / 5;
            double value = ly0 + (ly1 - ly0) * i / 5.0;
            double shown = logY ? Math.pow(10, value) : value;
            g.setColor(new Color(225, 225, 225));
            g.drawLine(left, y, left + plotWidth, y);
            g.setColor(Color.BLACK);
            g.drawString(format(shown), 22, y + 5);
        }
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(left, top, plotWidth, plotHeight);
        g.setFont(new Font("SansSerif", Font.PLAIN, 17));
        g.drawString(xLabel, left + plotWidth / 2 - 85, height - 25);
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -top - plotHeight / 2 - 100, 22);
        g.rotate(Math.PI / 2);
        for (Series item : series) {
            g.setColor(item.color());
            g.setStroke(new BasicStroke(2.6f));
            Point previous = null;
            for (Point point : item.points()) {
                double px = logX ? Math.log10(point.x()) : point.x();
                double py = logY ? Math.log10(Math.max(1, point.y())) : point.y();
                int x = left + (int) Math.round((px - lx0) / Math.max(1e-9, lx1 - lx0) * plotWidth);
                int y = top + plotHeight - (int) Math.round((py - ly0) / Math.max(1e-9, ly1 - ly0) * plotHeight);
                if (previous != null) {
                    double prevX = logX ? Math.log10(previous.x()) : previous.x();
                    double prevY = logY ? Math.log10(Math.max(1, previous.y())) : previous.y();
                    int x2 = left + (int) Math.round((prevX - lx0) / Math.max(1e-9, lx1 - lx0) * plotWidth);
                    int y2 = top + plotHeight - (int) Math.round((prevY - ly0) / Math.max(1e-9, ly1 - ly0) * plotHeight);
                    g.drawLine(x2, y2, x, y);
                }
                g.fillOval(x - 4, y - 4, 8, 8);
                previous = point;
            }
        }
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int legendX = left + plotWidth + 25;
        int legendY = top + 15;
        for (Series item : series) {
            g.setColor(item.color());
            g.setStroke(new BasicStroke(3f));
            g.drawLine(legendX, legendY - 4, legendX + 24, legendY - 4);
            g.setColor(Color.BLACK);
            g.drawString(item.label(), legendX + 32, legendY);
            legendY += 25;
        }
        g.dispose();
        ImageIO.write(image, "png", output.toFile());
    }

    private String format(double value) {
        if (value >= 1000) {
            return String.format("%.0fk", value / 1000.0);
        }
        if (value >= 10) {
            return String.format("%.0f", value);
        }
        return String.format("%.1f", value);
    }
}
