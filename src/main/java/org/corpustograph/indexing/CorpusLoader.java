package org.corpustograph.indexing;

import org.corpustograph.model.DocumentNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CorpusLoader {
    private final SimpleSummarizer summarizer = new SimpleSummarizer();

    public List<DocumentNode> load(Path directory) throws IOException {
        List<Path> files = Files.list(directory)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".txt") || p.toString().endsWith(".md"))
                .sorted(Comparator.comparing(Path::toString))
                .toList();

        List<DocumentNode> nodes = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            Path path = files.get(i);
            String content = Files.readString(path, StandardCharsets.UTF_8);
            double mass = Math.max(1.0, Math.sqrt(Math.max(1, content.length())) / 3.0);
            nodes.add(new DocumentNode(
                    i,
                    path.getFileName().toString(),
                    content,
                    summarizer.summarize(content, 20),
                    path,
                    mass
            ));
        }
        return nodes;
    }
}
