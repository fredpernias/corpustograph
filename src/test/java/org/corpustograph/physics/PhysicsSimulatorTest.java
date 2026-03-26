package org.corpustograph.physics;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhysicsSimulatorTest {

    @Test
    void simulationKeepsFiniteCoordinates() {
        DocumentNode a = new DocumentNode(0, "a", "x", "", Path.of("a"), 2);
        DocumentNode b = new DocumentNode(1, "b", "y", "", Path.of("b"), 3);
        GraphModel graph = new GraphModel(List.of(a, b), List.of(new SpringEdge(0, 1, 0.8, 0.1, 100)));

        PhysicsSimulator sim = new PhysicsSimulator(graph);
        for (int i = 0; i < 200; i++) {
            sim.step(0.1);
        }

        assertTrue(Double.isFinite(a.getX()));
        assertTrue(Double.isFinite(a.getY()));
        assertTrue(Double.isFinite(b.getX()));
        assertTrue(Double.isFinite(b.getY()));
    }
}
