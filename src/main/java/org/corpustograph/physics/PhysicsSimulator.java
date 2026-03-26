package org.corpustograph.physics;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.model.SpringEdge;

import java.util.List;
import java.util.Random;

public class PhysicsSimulator {
    private final GraphModel graph;
    private final Random random;
    private final double damping;
    private final double repulsion;
    private List<SpringEdge> activeEdges;

    public PhysicsSimulator(GraphModel graph) {
        this(graph, 0.98, 700.0, 42L);
    }

    public PhysicsSimulator(GraphModel graph, double damping, double repulsion, long seed) {
        this.graph = graph;
        this.damping = damping;
        this.repulsion = repulsion;
        this.random = new Random(seed);
        this.activeEdges = graph.edges();
        initializePositions();
    }

    public void setActiveEdges(List<SpringEdge> edges) {
        this.activeEdges = edges;
    }

    private void initializePositions() {
        for (DocumentNode node : graph.nodes()) {
            node.setPosition(100 + random.nextDouble() * 600, 100 + random.nextDouble() * 400);
            node.setVelocity(0, 0);
        }
    }

    public void step(double dt) {
        List<DocumentNode> nodes = graph.nodes();
        int n = nodes.size();
        double[] fx = new double[n];
        double[] fy = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                DocumentNode a = nodes.get(i);
                DocumentNode b = nodes.get(j);
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double dist2 = dx * dx + dy * dy + 0.01;
                double dist = Math.sqrt(dist2);
                double f = repulsion / dist2;
                double ux = dx / dist;
                double uy = dy / dist;
                fx[i] -= f * ux;
                fy[i] -= f * uy;
                fx[j] += f * ux;
                fy[j] += f * uy;
            }
        }

        for (SpringEdge edge : activeEdges) {
            DocumentNode a = nodes.get(edge.from());
            DocumentNode b = nodes.get(edge.to());
            double dx = b.getX() - a.getX();
            double dy = b.getY() - a.getY();
            double dist = Math.sqrt(dx * dx + dy * dy + 0.01);
            double stretch = dist - edge.restLength();
            double f = edge.stiffness() * stretch;
            double ux = dx / dist;
            double uy = dy / dist;

            fx[edge.from()] += f * ux;
            fy[edge.from()] += f * uy;
            fx[edge.to()] -= f * ux;
            fy[edge.to()] -= f * uy;
        }

        for (int i = 0; i < n; i++) {
            DocumentNode node = nodes.get(i);
            node.applyForce(fx[i], fy[i], dt);
            node.integrate(dt, damping);
        }
    }
}
