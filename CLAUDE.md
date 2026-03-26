# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
mvn -q exec:java   # Run the Swing GUI application
mvn test           # Run all tests
mvn test -Dtest=ClassName  # Run a single test class
mvn compile        # Compile only
```

## Architecture

CorpusToGraph is a Java 17 / Swing application that loads a directory of `.txt`/`.md` files, computes pairwise document similarity, and visualizes the corpus as a spring-particle graph with physics-based layout.

**Pipeline (orchestrated by `GraphBuilderService`):**

```
CorpusLoader → TextTokenizer → SimpleSummarizer
                             ↓
                    SimilarityEngine (BM25 or TF-IDF)
                             ↓
               GraphBuilderService builds edges from similarity matrix
                             ↓
               GraphPanel renders + PhysicsSimulator animates (60 FPS)
```

**Key design decisions:**
- `DocumentNode` is a mutable record — it holds both metadata (id, title, summary, mass) and live physics state (x, y, vx, vy). `applyForce()` and `integrate()` are called directly on it by `PhysicsSimulator`.
- `SpringEdge` maps similarity → spring stiffness and rest length: higher similarity = stiffer spring + shorter rest length.
- `SimilarityEngine` normalizes the output matrix to [0, 1] regardless of model chosen.
- The UI calls `PhysicsSimulator.step()` every 16 ms via a Swing `Timer`, scaled by the speed slider.

**Packages:**
- `ui` — `CorpusToGraphApp` (main window) + `GraphPanel` (rendering, click detection)
- `service` — `GraphBuilderService` (pipeline orchestration)
- `model` — `GraphModel`, `DocumentNode`, `SpringEdge`
- `physics` — `PhysicsSimulator` (repulsion + spring forces, Euler integration)
- `similarity` — `SimilarityEngine`, `SimilarityModel` enum (BM25, TF_IDF)
- `indexing` — `CorpusLoader`, `TextTokenizer`, `SimpleSummarizer`

## Tests

JUnit 5. Test resources (3 sample French docs) live in `src/test/resources/e2e/docs/`. The end-to-end test in `GraphBuilderServiceTest` runs the full pipeline including a physics simulation step.
