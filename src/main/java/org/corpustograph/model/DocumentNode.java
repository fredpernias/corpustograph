package org.corpustograph.model;

import java.nio.file.Path;

public class DocumentNode {
    private final int id;
    private final String title;
    private final String content;
    private final String summary;
    private final Path path;
    private final double mass;

    private double x;
    private double y;
    private double vx;
    private double vy;

    public DocumentNode(int id, String title, String content, String summary, Path path, double mass) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.path = path;
        this.mass = mass;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getSummary() { return summary; }
    public Path getPath() { return path; }
    public double getMass() { return mass; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public void setVelocity(double vx, double vy) { this.vx = vx; this.vy = vy; }

    public void applyForce(double fx, double fy, double dt) {
        double ax = fx / mass;
        double ay = fy / mass;
        vx += ax * dt;
        vy += ay * dt;
    }

    public void integrate(double dt, double damping) {
        vx *= damping;
        vy *= damping;
        x += vx * dt;
        y += vy * dt;
    }
}
