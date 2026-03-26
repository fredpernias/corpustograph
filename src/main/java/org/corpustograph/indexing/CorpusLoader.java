package org.corpustograph.indexing;

import org.corpustograph.model.DocumentNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CorpusLoader {
    private static final Set<String> EXCLUDED_DIRS = Set.of("target", ".git", "node_modules", ".idea", "__pycache__");

    private static final DocumentReader PLAIN = new PlainTextReader();
    private static final DocumentReader WORD  = new WordDocumentReader();

    private static final Map<String, DocumentReader> READERS = Map.ofEntries(
            Map.entry(".txt",        PLAIN),
            Map.entry(".md",         PLAIN),
            Map.entry(".java",       PLAIN),
            Map.entry(".py",         PLAIN),
            Map.entry(".js",         PLAIN),
            Map.entry(".ts",         PLAIN),
            Map.entry(".kt",         PLAIN),
            Map.entry(".go",         PLAIN),
            Map.entry(".rs",         PLAIN),
            Map.entry(".c",          PLAIN),
            Map.entry(".cpp",        PLAIN),
            Map.entry(".h",          PLAIN),
            Map.entry(".xml",        PLAIN),
            Map.entry(".html",       PLAIN),
            Map.entry(".json",       PLAIN),
            Map.entry(".yaml",       PLAIN),
            Map.entry(".yml",        PLAIN),
            Map.entry(".properties", PLAIN),
            Map.entry(".csv",        PLAIN),
            Map.entry(".doc",        WORD),
            Map.entry(".docx",       WORD)
    );

    private final SimpleSummarizer summarizer = new SimpleSummarizer();

    public List<DocumentNode> load(Path directory) throws IOException {
        List<Path> files = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(p -> !isExcluded(directory, p))
                .filter(p -> READERS.containsKey(extension(p)))
                .sorted(Comparator.comparing(Path::toString))
                .toList();

        List<DocumentNode> nodes = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            Path path = files.get(i);
            DocumentReader reader = READERS.get(extension(path));
            String content = reader.read(path);
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

    private static boolean isExcluded(Path root, Path path) {
        for (Path component : root.relativize(path)) {
            if (EXCLUDED_DIRS.contains(component.toString())) return true;
        }
        return false;
    }

    private static String extension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot).toLowerCase() : "";
    }
}
