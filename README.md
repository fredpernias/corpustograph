# CorpusToGraph (Java/Swing)

Application Java qui permet de:
- sélectionner un répertoire de documents (`.txt` / `.md`),
- calculer une similarité entre tous les documents (BM25 ou TF-IDF),
- transformer le corpus en graphe ressort-particule,
- lancer une animation de stabilisation,
- cliquer sur une particule pour voir un résumé.

## Modèle physique
- **Particule**: un document
- **Masse de la particule**: proportionnelle à la taille du document
- **Ressort entre deux particules**: une arête construite à partir de la similarité
  - plus la similarité est élevée, plus le ressort est rigide
  - plus la similarité est élevée, plus la longueur de repos est courte

## Lancer l'application
```bash
mvn -q exec:java
```

## Exécuter les tests
```bash
mvn test
```

## Fonctionnalités UI
- choix du répertoire
- choix du modèle: `BM25` ou `TF_IDF`
- slider de vitesse d'animation
- boutons Start/Stop
- panneau latéral de résumé document
