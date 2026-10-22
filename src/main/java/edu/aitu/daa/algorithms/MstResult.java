package edu.aitu.daa.algorithms;

import edu.aitu.daa.graph.Edge;
import edu.aitu.daa.metrics.Metrics;

import java.util.List;

public record MstResult(long totalWeight, int components, List<Edge> chosenEdges, Metrics metrics) {
}
