package org.corpustograph.ui;

import org.corpustograph.model.DocumentNode;
import org.corpustograph.model.GraphModel;
import org.corpustograph.service.GraphBuilderService;
import org.corpustograph.similarity.SimilarityModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;

public class CorpusToGraphApp {
    private final GraphBuilderService service = new GraphBuilderService();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CorpusToGraphApp().show());
    }

    private void show() {
        JFrame frame = new JFrame("CorpusToGraph - BM25/TF-IDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField folderField = new JTextField(35);
        JButton browse = new JButton("Parcourir");
        JComboBox<SimilarityModel> modelCombo = new JComboBox<>(SimilarityModel.values());
        JSlider speed = new JSlider(1, 400, 100);
        speed.setPreferredSize(new Dimension(150, 30));
        JButton load = new JButton("Charger corpus");
        JButton start = new JButton("Start");
        JButton stop = new JButton("Stop");

        controls.add(new JLabel("Répertoire:"));
        controls.add(folderField);
        controls.add(browse);
        controls.add(new JLabel("Modèle:"));
        controls.add(modelCombo);
        controls.add(new JLabel("Vitesse:"));
        controls.add(speed);
        controls.add(load);
        controls.add(start);
        controls.add(stop);

        GraphPanel graphPanel = new GraphPanel();
        JTextArea details = new JTextArea();
        details.setEditable(false);
        details.setLineWrap(true);
        details.setWrapStyleWord(true);
        details.setBorder(new EmptyBorder(8, 8, 8, 8));
        details.setText("Cliquez sur une particule pour voir un résumé.");

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, graphPanel, new JScrollPane(details));
        split.setResizeWeight(0.78);

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                folderField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        graphPanel.setOnNodeClick(node -> details.setText(formatNode(node)));
        speed.addChangeListener(e -> graphPanel.setSpeed(speed.getValue() / 100.0));

        load.addActionListener(e -> {
            try {
                SimilarityModel model = (SimilarityModel) modelCombo.getSelectedItem();
                GraphModel graph = service.fromDirectory(Path.of(folderField.getText()), model);
                graphPanel.setGraph(graph);
                details.setText("Corpus chargé: " + graph.nodes().size() + " documents.\nCliquez sur une particule.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        start.addActionListener(e -> graphPanel.start());
        stop.addActionListener(e -> graphPanel.stop());

        frame.setLayout(new BorderLayout());
        frame.add(controls, BorderLayout.NORTH);
        frame.add(split, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String formatNode(DocumentNode node) {
        return "Titre: " + node.getTitle() + "\n"
                + "Fichier: " + node.getPath() + "\n"
                + "Masse (taille doc): " + String.format("%.2f", node.getMass()) + "\n\n"
                + "Résumé:\n" + node.getSummary();
    }
}
