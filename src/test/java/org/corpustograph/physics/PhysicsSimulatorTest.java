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

    @Test
    void setActiveEdgesEmptyRemovesSpringAttraction() {
        // Deux nœuds placés loin de la longueur de repos (300 > 80) : le ressort les attire.
        // Sans arête active, seule la répulsion s'applique → ils s'éloignent.
        DocumentNode a = makeNode(0, 0, 0);
        DocumentNode b = makeNode(1, 300, 0);
        SpringEdge spring = new SpringEdge(0, 1, 0.9, 0.5, 80);
        GraphModel graph = new GraphModel(List.of(a, b), List.of(spring));

        PhysicsSimulator sim = new PhysicsSimulator(graph, 0.98, 100.0, 1L);
        a.setPosition(0, 0);
        b.setPosition(300, 0);

        // Avec ressort actif : les nœuds se rapprochent
        sim.setActiveEdges(List.of(spring));
        for (int i = 0; i < 60; i++) sim.step(0.1);
        double distWith = distance(a, b);
        assertTrue(distWith < 300, "Le ressort doit rapprocher les nœuds (dist=" + distWith + ")");

        // On retire le ressort et on repart de la même position
        a.setPosition(0, 0);
        b.setPosition(300, 0);
        a.setVelocity(0, 0);
        b.setVelocity(0, 0);
        sim.setActiveEdges(List.of());
        for (int i = 0; i < 60; i++) sim.step(0.1);
        double distWithout = distance(a, b);
        assertTrue(distWithout > 300, "Sans ressort, la répulsion doit écarter les nœuds (dist=" + distWithout + ")");
    }

    @Test
    void setActiveEdgesCanRestoreSpringAfterRemoval() {
        DocumentNode a = makeNode(0, 0, 0);
        DocumentNode b = makeNode(1, 300, 0);
        SpringEdge spring = new SpringEdge(0, 1, 0.9, 0.5, 80);
        GraphModel graph = new GraphModel(List.of(a, b), List.of(spring));

        PhysicsSimulator sim = new PhysicsSimulator(graph, 0.98, 50.0, 1L);

        // Phase 1 : sans ressort, les nœuds s'écartent
        a.setPosition(0, 0);
        b.setPosition(300, 0);
        a.setVelocity(0, 0);
        b.setVelocity(0, 0);
        sim.setActiveEdges(List.of());
        for (int i = 0; i < 30; i++) sim.step(0.1);
        double distAfterRemoval = distance(a, b);

        // Phase 2 : on remet le ressort → les nœuds se rapprochent
        sim.setActiveEdges(List.of(spring));
        for (int i = 0; i < 100; i++) sim.step(0.1);
        double distAfterRestore = distance(a, b);

        assertTrue(distAfterRestore < distAfterRemoval,
                "Après restauration du ressort, les nœuds doivent se rapprocher");
    }

    // --- helpers ---

    private static DocumentNode makeNode(int id, double x, double y) {
        DocumentNode n = new DocumentNode(id, "n" + id, "", "", Path.of("n" + id), 1.0);
        n.setPosition(x, y);
        n.setVelocity(0, 0);
        return n;
    }

    private static double distance(DocumentNode a, DocumentNode b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
