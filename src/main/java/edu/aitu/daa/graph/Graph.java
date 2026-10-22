package edu.aitu.daa.graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Graph {
    private final int vertexCount;
    private final boolean directed;
    private final List<Edge> edges;
    private final List<Edge>[] adjacency;

    @SuppressWarnings("unchecked")
    public Graph(int vertexCount, boolean directed) {
        if (vertexCount < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative");
        }
        this.vertexCount = vertexCount;
        this.directed = directed;
        this.edges = new ArrayList<>();
        this.adjacency = (List<Edge>[]) new List<?>[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            adjacency[i] = new ArrayList<>();
        }
    }

    public void addEdge(int u, int v, int weight) {
        checkVertex(u);
        checkVertex(v);
        Edge edge = new Edge(u, v, weight);
        edges.add(edge);
        adjacency[u].add(edge);
        if (!directed && v != u) {
            adjacency[v].add(edge);
        }
    }

    public int vertexCount() {
        return vertexCount;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    public List<Edge> outgoingEdges(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency[vertex]);
    }

    public int neighbor(Edge edge, int from) {
        if (directed) {
            if (edge.u() != from) {
                throw new IllegalArgumentException("Edge is not outgoing from this vertex");
            }
            return edge.v();
        }
        return edge.u() == from ? edge.v() : edge.u();
    }

    public Graph reverse() {
        if (!directed) {
            throw new IllegalStateException("Only directed graphs can be reversed");
        }
        Graph reversed = new Graph(vertexCount, true);
        for (Edge edge : edges) {
            reversed.addEdge(edge.v(), edge.u(), edge.weight());
        }
        return reversed;
    }

    public static Graph read(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        List<String> content = lines.stream().map(String::trim).filter(line -> !line.isEmpty()).toList();
        if (content.isEmpty()) {
            throw new IllegalArgumentException("Graph input is empty");
        }
        String[] header = content.get(0).split("\\s+");
        if (header.length != 3) {
            throw new IllegalArgumentException("First line must be: n m directed|undirected");
        }
        int n = Integer.parseInt(header[0]);
        int m = Integer.parseInt(header[1]);
        boolean directed;
        if (header[2].equalsIgnoreCase("directed")) {
            directed = true;
        } else if (header[2].equalsIgnoreCase("undirected")) {
            directed = false;
        } else {
            throw new IllegalArgumentException("Graph type must be directed or undirected");
        }
        if (m < 0 || content.size() != m + 1) {
            throw new IllegalArgumentException("Expected exactly " + m + " edge lines");
        }
        Graph graph = new Graph(n, directed);
        for (int i = 1; i < content.size(); i++) {
            String[] parts = content.get(i).split("\\s+");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Each edge line must contain u v w");
            }
            graph.addEdge(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }
        return graph;
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= vertexCount) {
            throw new IndexOutOfBoundsException("Invalid vertex: " + vertex);
        }
    }
}
