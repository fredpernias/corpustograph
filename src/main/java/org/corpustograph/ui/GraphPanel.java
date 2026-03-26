package org.corpustograph.ui;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;
import org.corpustograph.physics.PhysicsSimulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class GraphPanel extends JPanel {
    private GraphModel graph;
    private PhysicsSimulator simulator;
    private Timer timer;
    private Consumer<DocumentNode> onNodeClick;
    private double speed = 1.0;

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
                    if (d < best && d < 25) {
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
        this.simulator = new PhysicsSimulator(graph);
        repaint();
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

        for (SpringEdge edge : graph.edges()) {
            DocumentNode a = graph.nodes().get(edge.from());
            DocumentNode b = graph.nodes().get(edge.to());
            int alpha = (int) Math.min(255, 30 + edge.similarity() * 225);
            g2.setColor(new Color(70, 70, 70, alpha));
            g2.drawLine((int) a.getX(), (int) a.getY(), (int) b.getX(), (int) b.getY());
        }

        for (DocumentNode n : graph.nodes()) {
            int r = (int) Math.max(8, 4 + n.getMass() * 1.2);
            g2.setColor(new Color(0x3A86FF));
            g2.fillOval((int) n.getX() - r, (int) n.getY() - r, 2 * r, 2 * r);
            g2.setColor(Color.BLACK);
            g2.drawString(n.getTitle(), (int) n.getX() + r + 2, (int) n.getY() - 2);
        }
    }
}
