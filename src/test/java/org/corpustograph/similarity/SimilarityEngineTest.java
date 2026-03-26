package org.corpustograph.similarity;

import org.corpustograph.model.DocumentNode;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimilarityEngineTest {

    @Test
    void bm25MatrixIsSymmetricAndNormalized() {
        SimilarityEngine engine = new SimilarityEngine();
        List<DocumentNode> docs = List.of(
                new DocumentNode(0, "a", "chat noir rapide", "", Path.of("a"), 1),
                new DocumentNode(1, "b", "chat noir", "", Path.of("b"), 1),
                new DocumentNode(2, "c", "voiture rouge", "", Path.of("c"), 1)
        );

        double[][] m = engine.computeMatrix(docs, SimilarityModel.BM25);
        assertEquals(1.0, m[0][0], 1e-9);
        assertEquals(m[0][1], m[1][0], 1e-9);
        assertTrue(m[0][1] > m[0][2]);
        assertTrue(m[0][1] <= 1.0);
    }

    @Test
    void tfidfGivesHigherScoreToRelatedDocs() {
        SimilarityEngine engine = new SimilarityEngine();
        List<DocumentNode> docs = List.of(
                new DocumentNode(0, "a", "apple banana apple", "", Path.of("a"), 1),
                new DocumentNode(1, "b", "apple banana", "", Path.of("b"), 1),
                new DocumentNode(2, "c", "truck engine wheel", "", Path.of("c"), 1)
        );

        double[][] m = engine.computeMatrix(docs, SimilarityModel.TF_IDF);
        assertTrue(m[0][1] > m[0][2]);
    }
}
