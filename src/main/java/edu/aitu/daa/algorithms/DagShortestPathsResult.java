package edu.aitu.daa.algorithms;

import edu.aitu.daa.metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record DagShortestPathsResult(long[] distances, int[] parent, Metrics metrics) {
    public long sumDistances() {
        long sum = 0;
        for (long distance : distances) {
            if (distance != Long.MAX_VALUE) {
                sum += distance;
            }
        }
        return sum;
    }

    public List<Integer> restorePath(int target) {
        if (target < 0 || target >= parent.length) {
            throw new IndexOutOfBoundsException("Invalid target: " + target);
        }
        if (distances[target] == Long.MAX_VALUE) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        int current = target;
        int remaining = parent.length + 1;
        while (current != -1 && remaining-- > 0) {
            path.add(current);
            current = parent[current];
        }
        if (remaining < 0) {
            throw new IllegalStateException("Parent array contains a cycle");
        }
        Collections.reverse(path);
        return List.copyOf(path);
    }
}
