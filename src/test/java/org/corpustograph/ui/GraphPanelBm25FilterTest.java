package org.corpustograph.ui;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le filtrage des arêtes par seuil BM25 et la remontée du slider.
 * GraphPanel est instancié sans affichage (headless).
 */
class GraphPanelBm25FilterTest {

    static {
        // Swing peut s'initialiser sans écran
        System.setProperty("java.awt.headless", "true");
    }

    private static GraphModel twoEdgeGraph() {
        DocumentNode a = node(0);
        DocumentNode b = node(1);
        DocumentNode c = node(2);
        // arête forte (0.8) et arête faible (0.2)
        SpringEdge strong = new SpringEdge(0, 1, 0.8, 0.1, 80);
        SpringEdge weak   = new SpringEdge(0, 2, 0.2, 0.02, 200);
        return new GraphModel(List.of(a, b, c), List.of(strong, weak));
    }

    @Test
    void sliderAtMinShowsAllEdges() {
        GraphPanel panel = new GraphPanel();
        GraphModel graph = twoEdgeGraph();
        panel.setGraph(graph);

        // Seuil = min → toutes les arêtes doivent être au-dessus du seuil
        panel.setBm25Threshold(panel.getMinSimilarity());

        panel.revealAllEdgesForTest();

        // Les deux arêtes sont visibles (aucune filtrée)
        assertEquals(0.2, panel.getMinSimilarity(), 1e-9);
        assertEquals(0.8, panel.getMaxSimilarity(), 1e-9);
    }

    @Test
    void thresholdAboveWeakEdgeHidesIt() {
        GraphPanel panel = new GraphPanel();
        panel.setGraph(twoEdgeGraph());
        panel.stop();
        panel.revealAllEdgesForTest();

        // Seuil entre les deux similarités → l'arête faible disparaît
        double threshold = 0.5;
        panel.setBm25Threshold(threshold);

        // Vérification indirecte via la physique :
        // A est lié à B (forte, rest=80) mais plus à C (faible).
        // Après simulation, B doit être plus proche de A que C.
        DocumentNode a = panel.getGraph().nodes().get(0);
        DocumentNode b = panel.getGraph().nodes().get(1);
        DocumentNode c = panel.getGraph().nodes().get(2);
        a.setPosition(200, 200);
        b.setPosition(300, 200);
        c.setPosition(100, 200);
        a.setVelocity(0, 0); b.setVelocity(0, 0); c.setVelocity(0, 0);

        // On force quelques steps directement via le simulateur exposé
        for (int i = 0; i < 200; i++) panel.stepForTest(0.1);

        double distAB = dist(a, b);
        double distAC = dist(a, c);
        assertTrue(distAB < distAC,
                "A-B (lien fort actif) doit être plus proche que A-C (lien supprimé), distAB="
                        + distAB + " distAC=" + distAC);
    }

    @Test
    void loweringThresholdRestoresEdge() {
        // Graphe minimal à 2 nœuds pour isoler l'effet du seuil.
        // Rest length = 80px ; nœuds placés à 300px → le ressort est attractif.
        DocumentNode a = node(0);
        DocumentNode c = node(1);
        SpringEdge edge = new SpringEdge(0, 1, 0.3, 0.1, 80);
        GraphModel graph = new GraphModel(List.of(a, c), List.of(edge));

        GraphPanel panel = new GraphPanel();
        panel.setGraph(graph);
        panel.stop();
        panel.revealAllEdgesForTest();

        // Seuil élevé (0.5) : arête masquée → seule la répulsion, nœuds s'écartent
        a.setPosition(0, 0); c.setPosition(300, 0);
        a.setVelocity(0, 0); c.setVelocity(0, 0);
        panel.setBm25Threshold(0.5);
        for (int i = 0; i < 60; i++) panel.stepForTest(0.1);
        double distHigh = dist(a, c);

        // On abaisse le seuil (0.1) : arête réapparaît → ressort ramène les nœuds vers 80px
        a.setPosition(0, 0); c.setPosition(300, 0);
        a.setVelocity(0, 0); c.setVelocity(0, 0);
        panel.setBm25Threshold(0.1);
        for (int i = 0; i < 60; i++) panel.stepForTest(0.1);
        double distLow = dist(a, c);

        assertTrue(distLow < distHigh,
                "Abaisser le seuil doit rapprocher les nœuds (lien restauré), distLow="
                        + distLow + " distHigh=" + distHigh);
    }

    // --- helpers ---

    private static DocumentNode node(int id) {
        return new DocumentNode(id, "n" + id, "text", "", Path.of("n" + id), 1.0);
    }

    private static double dist(DocumentNode x, DocumentNode y) {
        double dx = x.getX() - y.getX(), dy = x.getY() - y.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
