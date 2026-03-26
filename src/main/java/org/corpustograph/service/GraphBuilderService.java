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

    public GraphModel fromDirectory(Path dir, SimilarityModel model) throws IOException {
        List<DocumentNode> docs = loader.load(dir);
        double[][] sim = similarityEngine.computeMatrix(docs, model);
        List<SpringEdge> edges = new ArrayList<>();

        for (int i = 0; i < docs.size(); i++) {
            for (int j = i + 1; j < docs.size(); j++) {
                double s = sim[i][j];
                double stiffness = 0.01 + s * 0.12;
                double restLength = 220 - 160 * s;
                edges.add(new SpringEdge(i, j, s, stiffness, Math.max(40, restLength)));
            }
        }
        return new GraphModel(docs, edges);
    }
}
