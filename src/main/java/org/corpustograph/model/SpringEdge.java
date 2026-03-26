package org.corpustograph.model;

public record SpringEdge(int from, int to, double similarity, double stiffness, double restLength) {}
