package edu.aitu.daa.benchmark;

import java.util.Map;

public final class ReferenceValues {
    public record Values(long mstWeight, int sccCount, long dagDistanceSum) {
    }

    private static final Map<String, Values> VALUES = Map.ofEntries(
            Map.entry("1000/4000", new Values(147970, 27, 396274)),
            Map.entry("1000/20000", new Values(30363, 1, 252411)),
            Map.entry("3162/12648", new Values(468911, 137, 1271911)),
            Map.entry("3162/63240", new Values(97589, 1, 723157)),
            Map.entry("10000/40000", new Values(1486139, 373, 3941828)),
            Map.entry("10000/200000", new Values(309550, 1, 2381342)),
            Map.entry("31623/126492", new Values(4715515, 1188, 12551891)),
            Map.entry("31623/632460", new Values(968517, 1, 7376612)),
            Map.entry("100000/400000", new Values(15012094, 3923, 40096539)),
            Map.entry("100000/2000000", new Values(3056681, 1, 23512087))
    );

    private ReferenceValues() {
    }

    public static Values get(int n, int m) {
        Values values = VALUES.get(n + "/" + m);
        if (values == null) {
            throw new IllegalArgumentException("No reference values for " + n + "/" + m);
        }
        return values;
    }
}
