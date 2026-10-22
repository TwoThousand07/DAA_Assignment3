package edu.aitu.daa;

import edu.aitu.daa.algorithms.Kahn;
import edu.aitu.daa.algorithms.TopologicalResult;
import edu.aitu.daa.graph.Graph;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TopologicalTest {
    @Test
    void returnsValidOrderForDag() throws Exception {
        Graph graph = Graph.read(Path.of("data/dag_negative.txt"));
        Kahn kahn = new Kahn();
        TopologicalResult result = kahn.run(graph);
        assertFalse(result.hasCycle());
        assertTrue(kahn.isValidOrder(graph, result.order()));
        assertTrue(result.metrics().getEdgeScans() == graph.edgeCount());
    }

    @Test
    void detectsCycleAndSelfLoop() throws Exception {
        Graph graph = Graph.read(Path.of("data/cyclic.txt"));
        assertTrue(new Kahn().run(graph).hasCycle());
        Graph selfLoop = new Graph(1, true);
        selfLoop.addEdge(0, 0, 3);
        assertTrue(new Kahn().run(selfLoop).hasCycle());
    }
}
