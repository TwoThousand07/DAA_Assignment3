package edu.aitu.daa.algorithms;

import edu.aitu.daa.metrics.Metrics;

public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;
    private final Metrics metrics;
    private int components;
    private long findCalls;
    private long successfulUnionCalls;

    public DisjointSetUnion(int n) {
        this(n, new Metrics());
    }

    public DisjointSetUnion(int n, Metrics metrics) {
        if (n < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        this.parent = new int[n];
        this.rank = new int[n];
        this.metrics = metrics;
        this.components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public void makeSet(int n) {
        if (n != parent.length) {
            throw new UnsupportedOperationException("This DSU is initialized with a fixed size");
        }
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
        components = n;
        findCalls = 0;
        successfulUnionCalls = 0;
    }

    public int find(int x) {
        checkIndex(x);
        findCalls++;
        metrics.addDsuOps(1);
        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        components--;
        successfulUnionCalls++;
        metrics.addDsuOps(1);
        return true;
    }

    public int components() {
        return components;
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnionCalls() {
        return successfulUnionCalls;
    }

    private void checkIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Invalid DSU index: " + x);
        }
    }
}
