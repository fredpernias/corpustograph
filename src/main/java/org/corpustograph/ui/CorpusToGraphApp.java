package org.corpustograph.ui;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.service.GraphBuilderService;
import org.corpustograph.similarity.SimilarityModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;
import java.util.List;

public class CorpusToGraphApp {
    static final int DEFAULT_MAX_DOCS = 100;
    private final GraphBuilderService service = new GraphBuilderService();

    public static void main(String[] args) throws Exception {
        String path = null;
        boolean headless = false;
        int maxDocs = DEFAULT_MAX_DOCS;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--headless"  -> headless = true;
                case "--path"      -> { if (i + 1 < args.length) path = args[++i]; }
                case "--max-docs"  -> { if (i + 1 < args.length) maxDocs = Integer.parseInt(args[++i]); }
            }
        }

        if (headless) {
            runHeadless(path, maxDocs);
        } else {
            final String finalPath = path;
            final int finalMax = maxDocs;
            SwingUtilities.invokeLater(() -> new CorpusToGraphApp().show(finalPath, finalMax));
        }
    }

    private static void runHeadless(String path, int maxDocs) throws Exception {
        if (path == null) {
            System.err.println("--headless requiert --path <répertoire>");
            System.exit(1);
        }
        GraphBuilderService service = new GraphBuilderService();
        System.out.println("Chargement: " + path);
        GraphBuilderService.LoadResult result = service.load(Path.of(path), maxDocs);
        List<DocumentNode> docs = result.docs();

        System.out.println("Documents chargés: " + docs.size()
                + "  (ignorés: " + result.skipped() + ")"
                + (docs.size() == maxDocs ? "  [limite " + maxDocs + " atteinte]" : ""));

        System.out.println("Calcul des similarités...");
        GraphModel graph = service.buildGraph(docs, SimilarityModel.BM25);
        System.out.println("Liens: " + graph.edges().size());
        for (DocumentNode n : graph.nodes()) {
            System.out.printf("  [%d] %s%n", n.getId(), n.getTitle());
        }
    }

    private void show(String initialPath, int maxDocs) {
        JFrame frame = new JFrame("CorpusToGraph - BM25/TF-IDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JTextField folderField = new JTextField(35);
        if (initialPath != null) folderField.setText(initialPath);
        JButton browse = new JButton("Parcourir");
        JComboBox<SimilarityModel> modelCombo = new JComboBox<>(SimilarityModel.values());
        JButton load = new JButton("Charger corpus");
        JButton stop = new JButton("Stop");

        JSlider speedSlider = new JSlider(1, 400, 100);
        speedSlider.setPreferredSize(new Dimension(120, 30));

        JSlider bm25Slider = new JSlider(0, 1000, 0);
        bm25Slider.setPreferredSize(new Dimension(150, 30));
        bm25Slider.setEnabled(false);
        JLabel bm25Label = new JLabel("Seuil: 0.000");

        controls.add(new JLabel("Répertoire:"));
        controls.add(folderField);
        controls.add(browse);
        controls.add(new JLabel("Modèle:"));
        controls.add(modelCombo);
        controls.add(new JLabel("Vitesse:"));
        controls.add(speedSlider);
        controls.add(load);
        controls.add(stop);
        controls.add(new JLabel("  Filtre BM25:"));
        controls.add(bm25Slider);
        controls.add(bm25Label);

        GraphPanel graphPanel = new GraphPanel();
        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBorder(new EmptyBorder(8, 8, 8, 8));
        details.setText("Cliquez sur une particule pour voir un résumé.");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                graphPanel, new JScrollPane(details));
        split.setResizeWeight(0.78);

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
        });

        graphPanel.setOnNodeClick(node -> details.setText(formatNode(node)));
        speedSlider.addChangeListener(e -> graphPanel.setSpeed(speedSlider.getValue() / 100.0));

        load.addActionListener(e -> {
            try {
                GraphBuilderService.LoadResult result = service.load(Path.of(folderField.getText()), maxDocs);
                List<DocumentNode> docs = result.docs();
                SimilarityModel model = (SimilarityModel) modelCombo.getSelectedItem();
                GraphModel graph = service.buildGraph(docs, model);
                graphPanel.setGraph(graph);

                double minSim = graphPanel.getMinSimilarity();
                double maxSim = graphPanel.getMaxSimilarity();
                bm25Slider.setValue(0);
                bm25Slider.setEnabled(true);
                bm25Label.setText(String.format("Seuil: %.3f", minSim));

                bm25Slider.addChangeListener(ev -> {
                    double threshold = minSim + (maxSim - minSim) * bm25Slider.getValue() / 1000.0;
                    bm25Label.setText(String.format("Seuil: %.3f", threshold));
                    graphPanel.setBm25Threshold(threshold);
                });

                details.setText("Corpus chargé: " + docs.size()
                        + " documents  (ignorés: " + result.skipped() + ")"
                        + (docs.size() == maxDocs ? "\n⚠ Limite " + maxDocs + " atteinte." : "") + "\n"
                        + "Cliquez sur une particule.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Erreur: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        stop.addActionListener(e -> graphPanel.stop());

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String formatNode(DocumentNode node) {
        return "Titre: " + node.getTitle() + "\n"
                + "Fichier: " + node.getPath() + "\n\n"
                + "Résumé:\n" + node.getSummary();
    }
}
