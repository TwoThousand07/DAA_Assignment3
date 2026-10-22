package edu.aitu.daa.benchmark;

import edu.aitu.daa.graph.Graph;

import java.util.Random;

public class GraphGenerator {
    public Graph undirected(int n, int m) {
        Random random = new Random(42);
        int[] permutation = sequence(n);
        shuffle(permutation, random);
        Graph graph = new Graph(n, false);
        for (int i = 1; i < n; i++) {
            int parent = permutation[random.nextInt(i)];
            graph.addEdge(parent, permutation[i], 1 + random.nextInt(1000));
        }
        for (int i = n - 1; i < m; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            while (b == a) {
                b = random.nextInt(n);
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }

    public Graph directed(int n, int m) {
        Random random = new Random(42);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            while (b == a) {
                b = random.nextInt(n);
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }

    public Graph dag(int n, int m) {
        Random random = new Random(42);
        int[] rank = sequence(n);
        shuffle(rank, random);
        Graph graph = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            while (b == a) {
                b = random.nextInt(n);
            }
            if (rank[a] > rank[b]) {
                int temp = a;
                a = b;
                b = temp;
            }
            graph.addEdge(a, b, 1 + random.nextInt(1000));
        }
        return graph;
    }

    private int[] sequence(int n) {
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = i;
        }
        return values;
    }

    private void shuffle(int[] values, Random random) {
        for (int i = values.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }
    }
}
