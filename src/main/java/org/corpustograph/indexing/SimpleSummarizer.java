package org.corpustograph.indexing;

import java.util.List;
import java.util.stream.Collectors;

public class SimpleSummarizer {
    private final TextTokenizer tokenizer = new TextTokenizer();

    public String summarize(String content, int maxWords) {
        List<String> words = tokenizer.tokenize(content);
        if (words.isEmpty()) {
            return "(document vide)";
        }
        return words.stream().limit(maxWords).collect(Collectors.joining(" "));
    }
}
