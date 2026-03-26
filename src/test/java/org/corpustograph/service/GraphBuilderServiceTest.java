package org.corpustograph.service;

import org.corpustograph.model.GraphModel;
import org.corpustograph.physics.PhysicsSimulator;
import org.corpustograph.similarity.SimilarityModel;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphBuilderServiceTest {

    @Test
    void endToEndBuildsGraphAndRunsSimulation() throws Exception {
        GraphBuilderService service = new GraphBuilderService();
        Path docsDir = resourcePath("e2e/docs");

        GraphModel graph = service.fromDirectory(docsDir, SimilarityModel.BM25);
        assertEquals(3, graph.nodes().size());
        assertEquals(3, graph.edges().size());

        assertFalse(graph.nodes().get(0).getSummary().isBlank());

        PhysicsSimulator sim = new PhysicsSimulator(graph);
        for (int i = 0; i < 100; i++) {
            sim.step(0.1);
        }

        assertTrue(Double.isFinite(graph.nodes().get(0).getX()));
    }

    private Path resourcePath(String relative) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(relative).toURI());
    }
}
