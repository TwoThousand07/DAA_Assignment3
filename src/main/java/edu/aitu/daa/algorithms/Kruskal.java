package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Kruskal {
    public MstResult run(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("MST algorithms require an undirected graph");
        }
        Metrics metrics = new Metrics();
        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount(), metrics);
        List<Edge> sortedEdges = new ArrayList<>(graph.edges());
        sortedEdges.sort((a, b) -> {
            metrics.addComparisons(1);
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> chosen = new ArrayList<>();
        long totalWeight = 0;
        int required = Math.max(0, graph.vertexCount() - 1);
        for (Edge edge : sortedEdges) {
            if (chosen.size() == required && graph.vertexCount() > 0) {
                break;
            }
            metrics.addEdgeScans(1);
            if (dsu.find(edge.u()) != dsu.find(edge.v())) {
                dsu.union(edge.u(), edge.v());
                chosen.add(edge);
                totalWeight += edge.weight();
            }
        }
        return new MstResult(totalWeight, dsu.components(), List.copyOf(chosen), metrics);
    }
}
