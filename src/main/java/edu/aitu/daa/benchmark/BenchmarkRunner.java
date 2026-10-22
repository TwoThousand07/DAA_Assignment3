package edu.aitu.daa.benchmark;

import edu.aitu.daa.algorithms.DagShortestPaths;
import edu.aitu.daa.algorithms.DagShortestPathsResult;
import edu.aitu.daa.algorithms.Kahn;
import edu.aitu.daa.algorithms.Kosaraju;
import edu.aitu.daa.algorithms.Kruskal;
import edu.aitu.daa.algorithms.MstResult;
import edu.aitu.daa.algorithms.Prim;
import edu.aitu.daa.algorithms.SccResult;
import edu.aitu.daa.algorithms.TopologicalResult;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;

public class BenchmarkRunner {
    private static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    private static final int RUNS = 5;
    private record Measurement<T>(T result, double medianMillis, Metrics metrics, long value) {
    }

    public void run() throws IOException {
        Path resultsDirectory = Path.of("results");
        Path csv = resultsDirectory.resolve("results.csv");
        Files.createDirectories(resultsDirectory);
        List<String> lines = new ArrayList<>();
        lines.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
        GraphGenerator generator = new GraphGenerator();
        Kruskal kruskal = new Kruskal();
        Prim prim = new Prim();
        Kosaraju kosaraju = new Kosaraju();
        Kahn kahn = new Kahn();
        DagShortestPaths dagShortestPaths = new DagShortestPaths();
        for (int n : SIZES) {
            for (int multiplier : new int[]{4, 20}) {
                int m = n * multiplier;
                String density = multiplier == 4 ? "sparse" : "dense";
                ReferenceValues.Values reference = ReferenceValues.get(n, m);
                System.out.printf("Generating and checking n=%d m=%d (%s)%n", n, m, density);

                Graph mstGraph = generator.undirected(n, m);
                Measurement<MstResult> kruskalRun = measure(() -> kruskal.run(mstGraph), MstResult::metrics, MstResult::totalWeight);
                Measurement<MstResult> primRun = measure(() -> prim.run(mstGraph), MstResult::metrics, MstResult::totalWeight);
                if (kruskalRun.value() != primRun.value()) {
                    throw new IllegalStateException("Kruskal and Prim disagree for " + n + "/" + m);
                }
                if (kruskalRun.result().chosenEdges().size() != n - kruskalRun.result().components()
                        || primRun.result().chosenEdges().size() != n - primRun.result().components()) {
                    throw new IllegalStateException("MST forest edge count is wrong for " + n + "/" + m);
                }
                if (kruskalRun.value() != reference.mstWeight()) {
                    throw mismatch("MST", n, m, reference.mstWeight(), kruskalRun.value());
                }
                addRow(lines, "Kruskal", "mst-" + density, n, m, kruskalRun);
                addRow(lines, "Prim", "mst-" + density, n, m, primRun);

                Graph directedGraph = generator.directed(n, m);
                Measurement<SccResult> sccRun = measure(() -> kosaraju.run(directedGraph), SccResult::metrics, SccResult::count);
                if (sccRun.value() != reference.sccCount()) {
                    throw mismatch("Kosaraju SCC count", n, m, reference.sccCount(), sccRun.value());
                }
                if (sccRun.metrics().getEdgeScans() != 2L * m) {
                    throw new IllegalStateException("Kosaraju must scan exactly 2m edges");
                }
                addRow(lines, "Kosaraju", "scc-" + density, n, m, sccRun);

                Graph dagGraph = generator.dag(n, m);
                Measurement<TopologicalResult> kahnRun = measure(() -> kahn.run(dagGraph), TopologicalResult::metrics,
                        value -> value.order().length);
                if (kahnRun.result().hasCycle() || !kahn.isValidOrder(dagGraph, kahnRun.result().order())) {
                    throw new IllegalStateException("Kahn returned an invalid order for a generated DAG");
                }
                if (kahnRun.metrics().getEdgeScans() != m) {
                    throw new IllegalStateException("Kahn must scan exactly m edges");
                }
                addRow(lines, "Kahn", "dag-" + density, n, m, kahnRun);

                Measurement<DagShortestPathsResult> pathRun = measure(() -> dagShortestPaths.run(dagGraph),
                        DagShortestPathsResult::metrics, DagShortestPathsResult::sumDistances);
                if (pathRun.value() != reference.dagDistanceSum()) {
                    throw mismatch("DAG distance sum", n, m, reference.dagDistanceSum(), pathRun.value());
                }
                if (pathRun.metrics().getEdgeScans() != m) {
                    throw new IllegalStateException("DAG relaxation must scan exactly m edges");
                }
                addRow(lines, "DagShortestPath", "dag-" + density, n, m, pathRun);
                System.out.printf("  MST=%d, SCC=%d, DAG distance sum=%d%n", kruskalRun.value(), sccRun.value(), pathRun.value());
            }
        }
        Files.write(csv, lines);
        if (lines.size() != 51) {
            throw new IllegalStateException("Expected 50 benchmark rows, found " + (lines.size() - 1));
        }
        new PlotGenerator().generate(csv, Path.of("results", "plots"));
        System.out.println("Wrote " + csv + " with 50 rows and 3 PNG plots.");
    }

    private <T> Measurement<T> measure(Supplier<T> action, Function<T, Metrics> metricsFunction, ToLongFunction<T> valueFunction) {
        long[] times = new long[RUNS];
        T first = null;
        Metrics metrics = null;
        long value = 0;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            T result = action.get();
            times[i] = System.nanoTime() - start;
            if (i == 0) {
                first = result;
                metrics = metricsFunction.apply(result);
                value = valueFunction.applyAsLong(result);
            } else {
                long currentValue = valueFunction.applyAsLong(result);
                if (currentValue != value) {
                    throw new IllegalStateException("Algorithm result changed between benchmark runs");
                }
            }
        }
        Arrays.sort(times);
        return new Measurement<>(first, times[RUNS / 2] / 1_000_000.0, metrics, value);
    }

    private void addRow(List<String> lines, String algorithm, String graphName, int n, int m, Measurement<?> run) {
        Metrics metrics = run.metrics();
        lines.add(String.format(java.util.Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                algorithm, graphName, n, m, run.medianMillis(), metrics.getDsuOps(), metrics.getEdgeScans(),
                metrics.getComparisons(), metrics.getMaxDepth(), run.value()));
    }

    private IllegalStateException mismatch(String label, int n, int m, long expected, long actual) {
        return new IllegalStateException(label + " reference mismatch for n=" + n + " m=" + m
                + ": expected " + expected + ", got " + actual);
    }
}
