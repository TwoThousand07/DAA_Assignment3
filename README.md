# DAA Assignment 3 - Graphs
## Build and tests

```bash
mvn clean test
```

## Run a small input graph

```bash
mvn exec:java -Dexec.args="run data/disconnected.txt"
```

To run a particular algorithm, add `kruskal`, `prim`, `kosaraju`, `kahn`, or `dag` after the input path. The file format is `n m directed|undirected` followed by `m` lines of `u v w`.

## Run the benchmark

```bash
mvn exec:java -Dexec.args="benchmark"
```

The benchmark generates the five required sizes and two densities with seed 42, runs each required algorithm five times, takes the median wall-clock time, checks MST agreement, checks topological order, checks the reference values, and writes `results/results.csv`. It also creates the three charts under `results/plots/`. To regenerate only the charts from an existing CSV, use `mvn exec:java -Dexec.args="plots"`. The benchmark can use substantial memory for the 100,000-vertex, 2,000,000-edge case; use a 64-bit JVM and allow enough heap, for example:

```bash
MAVEN_OPTS="-Xmx3g" mvn exec:java -Dexec.args="benchmark"
```

## Contents

- `src/main/java/edu/aitu/daa/graph`: graph and input reader
- `src/main/java/edu/aitu/daa/algorithms`: graph algorithms and DSU
- `src/main/java/edu/aitu/daa/metrics`: operation counters
- `src/main/java/edu/aitu/daa/benchmark`: deterministic graph generation, reference validation and benchmark
- `src/test/java/edu/aitu/daa`: JUnit 5 tests
- `data/`: example input graphs
- `results/results.csv`: benchmark results
- `results/plots/`: benchmark charts
- `REPORT.md`: complexity, correctness, algorithm selection and measured results

## Git workflow

The repository contains `main`, `feature/dsu`, `feature/mst`, `feature/scc`, `feature/paths`, and `feature/metrics`, with small feature commits and a `v1.0` release tag. To see the history, use `git log --oneline --decorate --graph --all`.