package org.corpustograph.ui;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;
import org.corpustograph.physics.PhysicsSimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

public class GraphPanel extends JPanel {
    private static final int NODE_RADIUS = 10;

    private GraphModel graph;
    private PhysicsSimulator simulator;
    private Timer timer;
    private Consumer<DocumentNode> onNodeClick;
    private double speed = 1.0;

    // arêtes triées par similarité décroissante (pour l'animation de création)
    private List<SpringEdge> allEdgesSorted = List.of();
    private int revealedCount = 0;
    private double bm25Threshold = 0.0;

    public GraphPanel() {
        setBackground(Color.WHITE);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (graph == null || onNodeClick == null) return;
                DocumentNode nearest = null;
                double best = Double.MAX_VALUE;
                for (DocumentNode n : graph.nodes()) {
                    double dx = n.getX() - e.getX();
                    double dy = n.getY() - e.getY();
                    double d = Math.sqrt(dx * dx + dy * dy);
                    if (d < best && d < NODE_RADIUS * 2) {
                        best = d;
                        nearest = n;
                    }
                }
                if (nearest != null) onNodeClick.accept(nearest);
            }
        });
    }

    public void setGraph(GraphModel graph) {
        this.graph = graph;
        this.allEdgesSorted = graph.edges().stream()
                .sorted(Comparator.comparingDouble(SpringEdge::similarity).reversed())
                .toList();
        this.revealedCount = 0;
        this.bm25Threshold = 0.0;
        this.simulator = new PhysicsSimulator(graph);
        this.simulator.setActiveEdges(List.of());
        start();
    }

    /** Nombre d'arêtes révélées par tick (≈ 3 secondes pour compléter l'animation). */
    private int edgesPerTick() {
        return Math.max(1, allEdgesSorted.size() / 180);
    }

    private void updateSimulatorEdges() {
        List<SpringEdge> active = allEdgesSorted.subList(0, revealedCount).stream()
                .filter(e -> e.similarity() >= bm25Threshold)
                .toList();
        simulator.setActiveEdges(active);
    }

    public void setBm25Threshold(double threshold) {
        this.bm25Threshold = threshold;
        updateSimulatorEdges();
        repaint();
    }

    /** Similarité minimale parmi toutes les arêtes (borne basse du slider). */
    public double getMinSimilarity() {
        return allEdgesSorted.isEmpty() ? 0.0 : allEdgesSorted.getLast().similarity();
    }

    /** Similarité maximale parmi toutes les arêtes (borne haute du slider). */
    public double getMaxSimilarity() {
        return allEdgesSorted.isEmpty() ? 1.0 : allEdgesSorted.getFirst().similarity();
    }

    public GraphModel getGraph() { return graph; }

    /** Avance la simulation d'un pas (utilisé par les tests sans Timer). */
    public void stepForTest(double dt) {
        if (simulator != null) simulator.step(dt);
    }

    /** Révèle immédiatement toutes les arêtes (utilisé par les tests). */
    public void revealAllEdgesForTest() {
        revealedCount = allEdgesSorted.size();
        updateSimulatorEdges();
    }

    public void setOnNodeClick(Consumer<DocumentNode> onNodeClick) {
        this.onNodeClick = onNodeClick;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void start() {
        if (simulator == null) return;
        if (timer != null) timer.stop();
        timer = new Timer(16, e -> {
            if (revealedCount < allEdgesSorted.size()) {
                revealedCount = Math.min(revealedCount + edgesPerTick(), allEdgesSorted.size());
                updateSimulatorEdges();
            }
            simulator.step(0.16 * speed);
            repaint();
        });
        timer.start();
    }

    public void stop() {
        if (timer != null) timer.stop();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (graph == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<SpringEdge> visible = allEdgesSorted.subList(0, revealedCount).stream()
                .filter(e -> e.similarity() >= bm25Threshold)
                .toList();

        for (SpringEdge edge : visible) {
            DocumentNode a = graph.nodes().get(edge.from());
            DocumentNode b = graph.nodes().get(edge.to());
            int alpha = (int) Math.min(255, 30 + edge.similarity() * 225);
            g2.setColor(new Color(70, 70, 70, alpha));
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
        }

        for (DocumentNode n : graph.nodes()) {
            g2.setColor(new Color(0x3A86FF));
            g2.fillOval((int) n.getX() - NODE_RADIUS, (int) n.getY() - NODE_RADIUS,
                    2 * NODE_RADIUS, 2 * NODE_RADIUS);
            g2.setColor(Color.BLACK);
            g2.drawString(n.getTitle(), (int) n.getX() + NODE_RADIUS + 2, (int) n.getY() - 2);
        }
    }
}
