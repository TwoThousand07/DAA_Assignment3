package edu.aitu.daa;

import edu.aitu.daa.algorithms.Kruskal;
import edu.aitu.daa.algorithms.MstResult;
import edu.aitu.daa.algorithms.Prim;
import edu.aitu.daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MstTest {
    @Test
    void bothAlgorithmsAgreeOnOneHundredConnectedGraphs() {
        Random random = new Random(8675309);
        Kruskal kruskal = new Kruskal();
        Prim prim = new Prim();
        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(20);
            Graph graph = new Graph(n, false);
            for (int vertex = 1; vertex < n; vertex++) {
                graph.addEdge(vertex, random.nextInt(vertex), random.nextInt(100) - 20);
            }
            int extraEdges = random.nextInt(n * 2 + 1);
            for (int i = 0; i < extraEdges; i++) {
                int u = random.nextInt(n);
                int v = random.nextInt(n);
                graph.addEdge(u, v, random.nextInt(100) - 20);
            }
            MstResult a = kruskal.run(graph);
            MstResult b = prim.run(graph);
            assertEquals(a.totalWeight(), b.totalWeight());
            assertEquals(1, a.components());
            assertEquals(1, b.components());
            assertEquals(n - 1, a.chosenEdges().size());
            assertEquals(n - 1, b.chosenEdges().size());
        }
    }

    @Test
    void disconnectedGraphReturnsMinimumSpanningForest() throws Exception {
        Graph graph = Graph.read(Path.of("data/disconnected.txt"));
        MstResult a = new Kruskal().run(graph);
        MstResult b = new Prim().run(graph);
        assertEquals(4, a.components());
        assertEquals(4, b.components());
        assertEquals(2, a.chosenEdges().size());
        assertEquals(2, b.chosenEdges().size());
        assertEquals(9, a.totalWeight());
        assertEquals(a.totalWeight(), b.totalWeight());
    }

    @Test
    void selfLoopsAndParallelEdgesDoNotBreakMst() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, 8);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, 4);
        MstResult result = new Kruskal().run(graph);
        assertEquals(6, result.totalWeight());
        assertEquals(2, result.chosenEdges().size());
        assertTrue(result.metrics().getEdgeScans() > 0);
    }
}
