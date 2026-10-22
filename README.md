# DAA Assignment 3 - Graphs

Java 17 and Maven project for the Design and Analysis of Algorithms assignment. It implements a shared adjacency-list graph, DSU, Kruskal, Prim, iterative Kosaraju, Kahn topological ordering, and DAG shortest paths.

## Open in IntelliJ IDEA

1. Extract the ZIP archive.
2. Open the `DAA_Assignment3` folder in IntelliJ IDEA and trust the Maven project.
3. Select JDK 17 or newer and reload Maven dependencies.
4. Run `edu.aitu.daa.Main` or use the Maven commands below.

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

## AI-use acknowledgement

AI assistance was used to help scaffold the project, review the implementation against the assignment checklist, and draft parts of the report. The student should run the tests and benchmark locally, inspect the generated results, and be ready to explain the algorithms and operation counters during the defense.
