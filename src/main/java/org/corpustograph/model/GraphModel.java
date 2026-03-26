package org.corpustograph.model;

import java.util.List;

public record GraphModel(List<DocumentNode> nodes, List<SpringEdge> edges) {}
