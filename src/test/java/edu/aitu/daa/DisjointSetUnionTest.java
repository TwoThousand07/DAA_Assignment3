package edu.aitu.daa;

import edu.aitu.daa.algorithms.DisjointSetUnion;
import edu.aitu.daa.metrics.Metrics;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisjointSetUnionTest {
    @Test
    void randomUnionsMatchSimpleReference() {
        int n = 150;
        DisjointSetUnion dsu = new DisjointSetUnion(n);
        int[] labels = new int[n];
        for (int i = 0; i < n; i++) {
            labels[i] = i;
        }
        Random random = new Random(42);
        for (int step = 0; step < 3000; step++) {
            int a = random.nextInt(n);
            int b = random.nextInt(n);
            boolean expected = labels[a] != labels[b];
            boolean actual = dsu.union(a, b);
            assertEquals(expected, actual);
            if (expected) {
                int oldLabel = labels[b];
                int newLabel = labels[a];
                for (int i = 0; i < n; i++) {
                    if (labels[i] == oldLabel) {
                        labels[i] = newLabel;
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                int x = random.nextInt(n);
                int y = random.nextInt(n);
                assertEquals(labels[x] == labels[y], dsu.find(x) == dsu.find(y));
            }
        }
        long distinct = java.util.Arrays.stream(labels).distinct().count();
        assertEquals(distinct, dsu.components());
        assertEquals(dsu.successfulUnionCalls(), n - distinct);
        assertTrue(dsu.findCalls() > 0);
    }

    @Test
    void countersAreIncrementedInsideOperations() {
        Metrics metrics = new Metrics();
        DisjointSetUnion dsu = new DisjointSetUnion(3, metrics);
        dsu.union(0, 1);
        dsu.union(1, 0);
        assertEquals(1, dsu.successfulUnionCalls());
        assertTrue(metrics.getDsuOps() >= 5);
    }
}
