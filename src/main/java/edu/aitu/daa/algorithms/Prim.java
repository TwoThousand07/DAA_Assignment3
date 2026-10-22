package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Prim {
    private record Candidate(int from, int to, int weight) {
    }

    public MstResult run(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("MST algorithms require an undirected graph");
        }
        Metrics metrics = new Metrics();
        int n = graph.vertexCount();
        boolean[] inTree = new boolean[n];
        PriorityQueue<Candidate> queue = new PriorityQueue<>((a, b) -> {
            metrics.addComparisons(1);
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> chosen = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;
        for (int start = 0; start < n; start++) {
            if (inTree[start]) {
                continue;
            }
            components++;
            inTree[start] = true;
            addCandidates(graph, start, inTree, queue, metrics);
            while (!queue.isEmpty()) {
                Candidate candidate = queue.poll();
                if (inTree[candidate.to()]) {
                    continue;
                }
                inTree[candidate.to()] = true;
                chosen.add(new Edge(candidate.from(), candidate.to(), candidate.weight()));
                totalWeight += candidate.weight();
                addCandidates(graph, candidate.to(), inTree, queue, metrics);
            }
        }
        return new MstResult(totalWeight, components, List.copyOf(chosen), metrics);
    }

    private void addCandidates(Graph graph, int from, boolean[] inTree, PriorityQueue<Candidate> queue, Metrics metrics) {
        for (Edge edge : graph.outgoingEdges(from)) {
            metrics.addEdgeScans(1);
            int to = graph.neighbor(edge, from);
            if (!inTree[to]) {
                queue.add(new Candidate(from, to, edge.weight()));
            }
        }
    }
}
