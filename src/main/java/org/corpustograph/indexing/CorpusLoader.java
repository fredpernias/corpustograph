package org.corpustograph.indexing;

import org.corpustograph.model.DocumentNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class CorpusLoader {
    private static final Set<String> EXCLUDED_DIRS = Set.of("target", ".git", "node_modules", ".idea", "__pycache__");
    private static final long MAX_FILE_BYTES = 5 * 1024 * 1024; // 5 Mo

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

    public record LoadResult(List<DocumentNode> docs, int skipped) {}

    private final SimpleSummarizer summarizer = new SimpleSummarizer();

    public LoadResult load(Path directory, int maxDocs) throws IOException {
        List<DocumentNode> nodes = new ArrayList<>();
        int skipped = 0;
        int id = 0;

        Iterable<Path> files = Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(p -> !isExcluded(directory, p))
                .filter(p -> READERS.containsKey(extension(p)))
                .filter(p -> fileSize(p) <= MAX_FILE_BYTES)
                .sorted(Comparator.comparing(Path::toString))
                ::iterator;

        for (Path path : files) {
            if (nodes.size() >= maxDocs) break;
            try {
                DocumentReader reader = READERS.get(extension(path));
                String content = reader.read(path);
                double mass = Math.max(1.0, Math.sqrt(Math.max(1, content.length())) / 3.0);
                nodes.add(new DocumentNode(
                        id++,
                        path.getFileName().toString(),
                        content,
                        summarizer.summarize(content, 20),
                        path,
                        mass
                ));
            } catch (Throwable e) {
                skipped++;
                System.err.println("Ignoré (erreur de lecture): " + path + " — " + e.getMessage());
            }
        }
        return new LoadResult(nodes, skipped);
    }

    private static long fileSize(Path path) {
        try { return Files.size(path); } catch (IOException e) { return Long.MAX_VALUE; }
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
