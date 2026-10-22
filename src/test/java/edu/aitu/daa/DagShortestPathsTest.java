package edu.aitu.daa;

import edu.aitu.daa.algorithms.DagShortestPaths;
import edu.aitu.daa.algorithms.DagShortestPathsResult;
import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DagShortestPathsTest {
    @Test
    void matchesBellmanFordOnRandomDagsWithNegativeWeights() {
        Random random = new Random(345);
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(15);
            Graph graph = new Graph(n, true);
            for (int u = 0; u < n; u++) {
                for (int v = u + 1; v < n; v++) {
                    if (random.nextDouble() < 0.24) {
                        graph.addEdge(u, v, random.nextInt(31) - 15);
                        if (random.nextDouble() < 0.08) {
                            graph.addEdge(u, v, random.nextInt(31) - 15);
                        }
                    }
                }
            }
            long[] expected = bellmanFordFromAllSources(graph);
            DagShortestPathsResult actual = new DagShortestPaths().run(graph);
            assertArrayEquals(expected, actual.distances());
            assertEquals(graph.edgeCount(), actual.metrics().getEdgeScans());
            for (int target = 0; target < n; target++) {
                if (actual.distances()[target] != Long.MAX_VALUE) {
                    assertEquals(target, actual.restorePath(target).get(actual.restorePath(target).size() - 1).intValue());
                }
            }
        }
    }

    @Test
    void negativeWeightInputRestoresPath() throws Exception {
        Graph graph = Graph.read(Path.of("data/dag_negative.txt"));
        DagShortestPathsResult result = new DagShortestPaths().run(graph);
        assertEquals(-2, result.sumDistances());
        assertEquals(Arrays.asList(1, 3, 5), result.restorePath(5));
        assertTrue(result.distances()[3] < 0);
    }

    @Test
    void rejectsCyclicGraph() {
        Graph graph = new Graph(2, true);
        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 0, 1);
        assertThrows(IllegalArgumentException.class, () -> new DagShortestPaths().run(graph));
    }

    private long[] bellmanFordFromAllSources(Graph graph) {
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.edges()) {
            indegree[edge.v()]++;
        }
        long[] distance = new long[n];
        Arrays.fill(distance, Long.MAX_VALUE);
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                distance[i] = 0;
            }
        }
        for (int i = 0; i < n - 1; i++) {
            boolean changed = false;
            for (Edge edge : graph.edges()) {
                if (distance[edge.u()] != Long.MAX_VALUE) {
                    long candidate = distance[edge.u()] + edge.weight();
                    if (candidate < distance[edge.v()]) {
                        distance[edge.v()] = candidate;
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }
        return distance;
    }
}
