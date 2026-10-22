package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.util.Arrays;

public class DagShortestPaths {
    public DagShortestPathsResult run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("DAG shortest paths require a directed graph");
        }
        Kahn kahn = new Kahn();
        TopologicalResult topological = kahn.run(graph);
        if (topological.hasCycle()) {
            throw new IllegalArgumentException("Shortest paths are only supported for DAGs");
        }
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.edges()) {
            indegree[edge.v()]++;
        }
        long[] distance = new long[n];
        int[] parent = new int[n];
        Arrays.fill(distance, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                distance[i] = 0;
            }
        }
        Metrics metrics = new Metrics();
        for (int vertex : topological.order()) {
            if (distance[vertex] == Long.MAX_VALUE) {
                continue;
            }
            for (Edge edge : graph.outgoingEdges(vertex)) {
                metrics.addEdgeScans(1);
                metrics.addComparisons(1);
                int next = graph.neighbor(edge, vertex);
                long candidate = distance[vertex] + edge.weight();
                if (candidate < distance[next]) {
                    distance[next] = candidate;
                    parent[next] = vertex;
                }
            }
        }
        return new DagShortestPathsResult(distance, parent, metrics);
    }
}
