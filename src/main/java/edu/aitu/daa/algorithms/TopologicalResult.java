package edu.aitu.daa.algorithms;

import edu.aitu.daa.metrics.Metrics;

public record TopologicalResult(int[] order, boolean hasCycle, Metrics metrics) {
}
