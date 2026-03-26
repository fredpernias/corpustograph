package org.corpustograph.indexing;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class WordDocumentReader implements DocumentReader {
    @Override
    public String read(Path path) throws IOException {
        String name = path.getFileName().toString().toLowerCase();
        try (InputStream in = Files.newInputStream(path)) {
            if (name.endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(in)) {
                    return doc.getParagraphs().stream()
                            .map(XWPFParagraph::getText)
                            .collect(Collectors.joining("\n"));
                }
            } else {
                try (HWPFDocument doc = new HWPFDocument(in)) {
                    return doc.getDocumentText();
                }
            }
        }
    }
}
