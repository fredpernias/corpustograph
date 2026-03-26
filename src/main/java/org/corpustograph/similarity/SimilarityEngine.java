package org.corpustograph.similarity;

import org.corpustograph.indexing.TextTokenizer;
import org.corpustograph.model.DocumentNode;

import java.util.*;

public class SimilarityEngine {
    private final TextTokenizer tokenizer = new TextTokenizer();

    public double[][] computeMatrix(List<DocumentNode> docs, SimilarityModel model) {
        int n = docs.size();
        double[][] matrix = new double[n][n];

        List<List<String>> tokens = docs.stream()
                .map(d -> tokenizer.tokenize(d.getContent()))
                .toList();

        // Précalcul partagé : df (document frequency) et avgdl
        Map<String, Integer> df = computeDf(tokens);
        double avgdl = tokens.stream().mapToInt(List::size).average().orElse(1.0);

        for (int i = 0; i < n; i++) {
            matrix[i][i] = 1.0;
            for (int j = i + 1; j < n; j++) {
                double score = switch (model) {
                    case BM25    -> bm25Symmetric(tokens, i, j, df, avgdl, n);
                    case TF_IDF  -> tfidfCosine(tokens, i, j, df, n);
                };
                matrix[i][j] = score;
                matrix[j][i] = score;
            }
        }
        return normalize(matrix);
    }

    /** Nombre de documents contenant chaque terme. */
    private Map<String, Integer> computeDf(List<List<String>> tokenizedDocs) {
        Map<String, Integer> df = new HashMap<>();
        for (List<String> doc : tokenizedDocs) {
            for (String term : new HashSet<>(doc)) {
                df.merge(term, 1, Integer::sum);
            }
        }
        return df;
    }

    private double bm25Symmetric(List<List<String>> docs, int a, int b,
                                  Map<String, Integer> df, double avgdl, int N) {
        return (bm25(docs.get(a), docs.get(b), df, avgdl, N)
              + bm25(docs.get(b), docs.get(a), df, avgdl, N)) / 2.0;
    }

    private double bm25(List<String> query, List<String> doc,
                        Map<String, Integer> df, double avgdl, int N) {
        final double k1 = 1.2;
        final double b  = 0.75;

        Map<String, Long> tf = frequencies(doc);
        Map<String, Long> qf = frequencies(query);
        int dl = doc.size();

        double score = 0.0;
        for (String term : qf.keySet()) {
            int ni = df.getOrDefault(term, 0);
            if (ni == 0) continue;
            double idf = Math.log(1 + (N - ni + 0.5) / (ni + 0.5));
            double f = tf.getOrDefault(term, 0L);
            double numerator   = f * (k1 + 1);
            double denominator = f + k1 * (1 - b + b * (dl / avgdl));
            if (denominator > 0) score += idf * (numerator / denominator);
        }
        return score;
    }

    private double tfidfCosine(List<List<String>> docs, int a, int b,
                               Map<String, Integer> df, int N) {
        Map<String, Long> fa = frequencies(docs.get(a));
        Map<String, Long> fb = frequencies(docs.get(b));

        Set<String> vocab = new HashSet<>(fa.keySet());
        vocab.addAll(fb.keySet());

        double dot = 0, na = 0, nb = 0;
        for (String term : vocab) {
            double idf = Math.log(1 + (double) N / (1 + df.getOrDefault(term, 0)));
            double wa = fa.getOrDefault(term, 0L) * idf;
            double wb = fb.getOrDefault(term, 0L) * idf;
            dot += wa * wb;
            na  += wa * wa;
            nb  += wb * wb;
        }
        if (na == 0 || nb == 0) return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private Map<String, Long> frequencies(List<String> tokens) {
        Map<String, Long> map = new HashMap<>();
        for (String t : tokens) map.merge(t, 1L, Long::sum);
        return map;
    }

    private double[][] normalize(double[][] matrix) {
        double max = 0;
        for (double[] row : matrix) for (double v : row) max = Math.max(max, v);
        if (max <= 0) return matrix;
        int n = matrix.length;
        double[][] out = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                out[i][j] = matrix[i][j] / max;
        return out;
    }
}
