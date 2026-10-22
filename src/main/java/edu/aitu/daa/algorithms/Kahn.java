package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class Kahn {
    public TopologicalResult run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological ordering requires a directed graph");
        }
        Metrics metrics = new Metrics();
        int n = graph.vertexCount();
        int[] indegree = new int[n];
        for (Edge edge : graph.edges()) {
            indegree[edge.v()]++;
        }
        Queue<Integer> queue = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (indegree[i] == 0) {
                queue.add(i);
            }
        }
        int[] order = new int[n];
        int size = 0;
        while (!queue.isEmpty()) {
            int vertex = queue.remove();
            order[size++] = vertex;
            for (Edge edge : graph.outgoingEdges(vertex)) {
                metrics.addEdgeScans(1);
                int next = graph.neighbor(edge, vertex);
                indegree[next]--;
                if (indegree[next] == 0) {
                    queue.add(next);
                }
            }
        }
        return new TopologicalResult(Arrays.copyOf(order, size), size != n, metrics);
    }

    public boolean isValidOrder(Graph graph, int[] order) {
        if (graph.vertexCount() != order.length) {
            return false;
        }
        int[] position = new int[order.length];
        boolean[] seen = new boolean[order.length];
        for (int i = 0; i < order.length; i++) {
            int vertex = order[i];
            if (vertex < 0 || vertex >= order.length || seen[vertex]) {
                return false;
            }
            seen[vertex] = true;
            position[vertex] = i;
        }
        for (Edge edge : graph.edges()) {
            if (position[edge.u()] >= position[edge.v()]) {
                return false;
            }
        }
        return true;
    }
}
