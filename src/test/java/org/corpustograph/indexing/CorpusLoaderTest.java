package org.corpustograph.indexing;

import org.corpustograph.model.DocumentNode;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorpusLoaderTest {

    @Test
    void loadRecursivelyFindsFilesInSubdirectories() throws Exception {
        Path docsDir = resourcePath("e2e/docs");
        List<DocumentNode> nodes = new CorpusLoader().load(docsDir);

        // 3 fichiers racine + 2 fichiers dans subdir
        assertEquals(5, nodes.size());

        List<String> titles = nodes.stream().map(DocumentNode::getTitle).toList();
        assertTrue(titles.contains("doc4.txt"), "doc4.txt (subdir) doit être chargé");
        assertTrue(titles.contains("doc5.md"),  "doc5.md  (subdir) doit être chargé");
    }

    @Test
    void loadIgnoresNonTextFiles() throws Exception {
        Path docsDir = resourcePath("e2e/docs");
        List<DocumentNode> nodes = new CorpusLoader().load(docsDir);

        nodes.forEach(n -> {
            String t = n.getTitle();
            assertTrue(t.endsWith(".txt") || t.endsWith(".md"),
                    "Seuls .txt et .md doivent être chargés, trouvé : " + t);
        });
    }

    private Path resourcePath(String relative) throws URISyntaxException {
        return Path.of(getClass().getClassLoader().getResource(relative).toURI());
    }
}
