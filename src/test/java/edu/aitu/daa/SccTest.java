package edu.aitu.daa;

import edu.aitu.daa.algorithms.Kosaraju;
import edu.aitu.daa.algorithms.SccResult;
import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SccTest {
    @Test
    void kosarajuMatchesBruteForceOnRandomGraphs() {
        Random random = new Random(42);
        for (int trial = 0; trial < 80; trial++) {
            int n = 1 + random.nextInt(12);
            Graph graph = new Graph(n, true);
            for (int i = 0; i < n * 2; i++) {
                graph.addEdge(random.nextInt(n), random.nextInt(n), random.nextInt(20) - 10);
            }
            boolean[][] reach = transitiveReachability(graph);
            SccResult kosaraju = new Kosaraju().run(graph);
            assertEquals(bruteCount(reach), kosaraju.count());
            assertSamePartition(kosaraju, reach, n);
        }
    }

    @Test
    void stackSafeAtOneHundredThousandVertices() {
        int n = 100_000;
        Graph graph = new Graph(n, true);
        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1);
        }
        SccResult result = new Kosaraju().run(graph);
        assertEquals(n, result.count());
        assertEquals(n - 1L, result.metrics().getEdgeScans() / 2);
        assertTrue(result.metrics().getMaxDepth() >= n - 1L);
    }

    @Test
    void sampleCyclicGraphHasThreeSccs() throws Exception {
        Graph graph = Graph.read(Path.of("data/cyclic.txt"));
        assertEquals(3, new Kosaraju().run(graph).count());
    }

    private boolean[][] transitiveReachability(Graph graph) {
        int n = graph.vertexCount();
        boolean[][] reach = new boolean[n][n];
        for (int source = 0; source < n; source++) {
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(source);
            reach[source][source] = true;
            while (!queue.isEmpty()) {
                int vertex = queue.remove();
                for (Edge edge : graph.outgoingEdges(vertex)) {
                    int next = graph.neighbor(edge, vertex);
                    if (!reach[source][next]) {
                        reach[source][next] = true;
                        queue.add(next);
                    }
                }
            }
        }
        return reach;
    }

    private int bruteCount(boolean[][] reach) {
        int n = reach.length;
        boolean[] assigned = new boolean[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (assigned[i]) {
                continue;
            }
            count++;
            for (int j = i; j < n; j++) {
                if (reach[i][j] && reach[j][i]) {
                    assigned[j] = true;
                }
            }
        }
        return count;
    }

    private void assertSamePartition(SccResult result, boolean[][] reach, int n) {
        int[] component = new int[n];
        for (int i = 0; i < result.components().size(); i++) {
            for (int vertex : result.components().get(i)) {
                component[vertex] = i;
            }
        }
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                assertEquals(reach[u][v] && reach[v][u], component[u] == component[v]);
            }
        }
    }
}
