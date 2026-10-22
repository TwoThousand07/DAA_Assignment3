package edu.aitu.daa.metrics;

public class Metrics {
    private long dsuOps;
    private long edgeScans;
    private long comparisons;
    private long maxDepth;

    public long getDsuOps() {
        return dsuOps;
    }

    public long getEdgeScans() {
        return edgeScans;
    }

    public long getComparisons() {
        return comparisons;
    }

    public long getMaxDepth() {
        return maxDepth;
    }

    public void addDsuOps(long value) {
        dsuOps += value;
    }

    public void addEdgeScans(long value) {
        edgeScans += value;
    }

    public void addComparisons(long value) {
        comparisons += value;
    }

    public void recordDepth(long depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }
}
