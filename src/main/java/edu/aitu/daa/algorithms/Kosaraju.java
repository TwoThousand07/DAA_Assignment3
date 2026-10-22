package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.graph.Graph;
import edu.aitu.daa.metrics.Metrics;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class Kosaraju {
    private static final class Frame {
        private final int vertex;
        private int nextEdge;

        private Frame(int vertex) {
            this.vertex = vertex;
        }
    }

    public SccResult run(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("SCC algorithms require a directed graph");
        }
        Metrics metrics = new Metrics();
        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finishCount = 0;
        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }
            Deque<Frame> stack = new ArrayDeque<>();
            visited[start] = true;
            stack.push(new Frame(start));
            metrics.recordDepth(stack.size());
            while (!stack.isEmpty()) {
                Frame frame = stack.peek();
                List<Edge> outgoing = graph.outgoingEdges(frame.vertex);
                if (frame.nextEdge < outgoing.size()) {
                    Edge edge = outgoing.get(frame.nextEdge++);
                    metrics.addEdgeScans(1);
                    int next = graph.neighbor(edge, frame.vertex);
                    if (!visited[next]) {
                        visited[next] = true;
                        stack.push(new Frame(next));
                        metrics.recordDepth(stack.size());
                    }
                } else {
                    finishOrder[finishCount++] = frame.vertex;
                    stack.pop();
                }
            }
        }
        Graph reversed = graph.reverse();
        Arrays.fill(visited, false);
        List<List<Integer>> components = new ArrayList<>();
        for (int i = finishCount - 1; i >= 0; i--) {
            int start = finishOrder[i];
            if (visited[start]) {
                continue;
            }
            List<Integer> component = new ArrayList<>();
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(start);
            visited[start] = true;
            metrics.recordDepth(stack.size());
            while (!stack.isEmpty()) {
                int vertex = stack.pop();
                component.add(vertex);
                for (Edge edge : reversed.outgoingEdges(vertex)) {
                    metrics.addEdgeScans(1);
                    int next = reversed.neighbor(edge, vertex);
                    if (!visited[next]) {
                        visited[next] = true;
                        stack.push(next);
                        metrics.recordDepth(stack.size());
                    }
                }
            }
            components.add(List.copyOf(component));
        }
        return new SccResult(List.copyOf(components), metrics);
    }
}
