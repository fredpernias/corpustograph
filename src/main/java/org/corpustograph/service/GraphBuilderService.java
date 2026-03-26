package org.corpustograph.service;

import org.corpustograph.indexing.CorpusLoader;
import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;
import org.corpustograph.similarity.SimilarityEngine;
import org.corpustograph.similarity.SimilarityModel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GraphBuilderService {
    private final CorpusLoader loader = new CorpusLoader();
    private final SimilarityEngine similarityEngine = new SimilarityEngine();

    public record LoadResult(List<DocumentNode> docs, int skipped) {}

    public LoadResult load(Path dir, int maxDocs) throws IOException {
        CorpusLoader.LoadResult r = loader.load(dir, maxDocs);
        return new LoadResult(r.docs(), r.skipped());
    }

    public GraphModel buildGraph(List<DocumentNode> docs, SimilarityModel model) {
        double[][] sim = similarityEngine.computeMatrix(docs, model);
        List<SpringEdge> edges = new ArrayList<>();
        // Raideur uniforme ; longueur au repos inversement proportionnelle à la similarité.
        // Formule : C / (s + ε)  → ~400px pour s=0, ~44px pour s=1 (avec C=50, ε=0.125).
        final double STIFFNESS = 0.05;
        final double C = 50.0;
        final double EPS = 0.125;
        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                double s = sim[i][j];
                double restLength = C / (s + EPS);
                edges.add(new SpringEdge(i, j, s, STIFFNESS, restLength));
            }
        }
        return new GraphModel(docs, edges);
    }

    public GraphModel fromDirectory(Path dir, SimilarityModel model) throws IOException {
        return buildGraph(load(dir, Integer.MAX_VALUE).docs(), model);
    }
}
