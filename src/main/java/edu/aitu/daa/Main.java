package edu.aitu.daa;

import edu.aitu.daa.algorithms.DagShortestPaths;
import edu.aitu.daa.algorithms.DagShortestPathsResult;
import edu.aitu.daa.algorithms.Kahn;
import edu.aitu.daa.algorithms.Kosaraju;
import edu.aitu.daa.algorithms.Kruskal;
import edu.aitu.daa.algorithms.MstResult;
import edu.aitu.daa.algorithms.Prim;
import edu.aitu.daa.algorithms.SccResult;
import edu.aitu.daa.algorithms.TopologicalResult;
import edu.aitu.daa.benchmark.BenchmarkRunner;
import edu.aitu.daa.benchmark.PlotGenerator;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        if (args.length == 0) {
            printUsage();
            return;
        }
        if (args[0].equalsIgnoreCase("benchmark")) {
            new BenchmarkRunner().run();
            return;
        }
        if (args[0].equalsIgnoreCase("plots")) {
            new PlotGenerator().generate(Path.of("results", "results.csv"), Path.of("results", "plots"));
            System.out.println("Wrote three plots under results/plots");
            return;
        }
        if (!args[0].equalsIgnoreCase("run") || args.length < 2) {
            printUsage();
            return;
        }
        Graph graph = Graph.read(Path.of(args[1]));
        String algorithm = args.length >= 3 ? args[2].toLowerCase() : "all";
        if (algorithm.equals("all")) {
            runAll(graph);
        } else {
            runSelected(graph, algorithm);
        }
    }

    private static void runAll(Graph graph) {
        System.out.printf("Graph: vertices=%d, edges=%d, type=%s%n", graph.vertexCount(), graph.edgeCount(),
                graph.isDirected() ? "directed" : "undirected");
        if (graph.isDirected()) {
            printScc(new Kosaraju().run(graph), "Kosaraju");
            TopologicalResult topological = new Kahn().run(graph);
            printTopological(topological);
            if (!topological.hasCycle()) {
                printPaths(new DagShortestPaths().run(graph));
            }
        } else {
            printMst(new Kruskal().run(graph), "Kruskal");
            printMst(new Prim().run(graph), "Prim");
        }
    }

    private static void runSelected(Graph graph, String algorithm) {
        switch (algorithm) {
            case "kruskal" -> printMst(new Kruskal().run(graph), "Kruskal");
            case "prim" -> printMst(new Prim().run(graph), "Prim");
            case "kosaraju" -> printScc(new Kosaraju().run(graph), "Kosaraju");
            case "kahn" -> printTopological(new Kahn().run(graph));
            case "dag" -> printPaths(new DagShortestPaths().run(graph));
            default -> throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }

    private static void printMst(MstResult result, String name) {
        System.out.printf("%s: weight=%d, components=%d, edges=%d%n", name, result.totalWeight(), result.components(), result.chosenEdges().size());
        printMetrics(result.metrics());
    }

    private static void printScc(SccResult result, String name) {
        System.out.printf("%s: components=%d%n", name, result.count());
        printMetrics(result.metrics());
    }

    private static void printTopological(TopologicalResult result) {
        if (result.hasCycle()) {
            System.out.println("Kahn: cycle detected; no topological order exists");
        } else {
            System.out.println("Kahn order: " + Arrays.toString(result.order()));
        }
        printMetrics(result.metrics());
    }

    private static void printPaths(DagShortestPathsResult result) {
        System.out.println("DAG shortest-path distances: " + Arrays.toString(result.distances()));
        System.out.println("Sum of distances: " + result.sumDistances());
        if (result.distances().length > 0) {
            int target = result.distances().length - 1;
            System.out.println("Path to vertex " + target + ": " + result.restorePath(target));
        }
        printMetrics(result.metrics());
    }

    private static void printMetrics(Metrics metrics) {
        System.out.printf("Metrics: dsuOps=%d, edgeScans=%d, comparisons=%d, maxDepth=%d%n",
                metrics.getDsuOps(), metrics.getEdgeScans(), metrics.getComparisons(), metrics.getMaxDepth());
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  run <path-to-graph> [kruskal|prim|kosaraju|kahn|dag]");
        System.out.println("  benchmark");
        System.out.println("  plots");
    }
}
