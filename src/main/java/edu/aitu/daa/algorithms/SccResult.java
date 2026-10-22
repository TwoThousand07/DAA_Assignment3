package edu.aitu.daa.algorithms;

import edu.aitu.daa.metrics.Metrics;

import java.util.List;

public record SccResult(List<List<Integer>> components, Metrics metrics) {
    public int count() {
        return components.size();
    }
}
