package de.netzfactor.servicedesk.ki;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/** Legt das Handbuch beim Start in den Vektorspeicher - vorher kann der Assistent nichts daraus beantworten. */
@ApplicationScoped
public class Handbuch {

    private static final Logger LOG = Logger.getLogger(Handbuch.class);

    private final EmbeddingStore<TextSegment> speicher;
    private final EmbeddingModel einbettung;
    private final String pfad;
    private final Optional<String> schluessel;

    private int abschnitte;

    public Handbuch(EmbeddingStore<TextSegment> speicher,
                    EmbeddingModel einbettung,
                    @ConfigProperty(name = "servicedesk.handbuch.pfad", defaultValue = "handbuch") String pfad,
                    @ConfigProperty(name = "quarkus.langchain4j.openai.api-key") Optional<String> schluessel) {
        this.speicher = speicher;
        this.einbettung = einbettung;
        this.pfad = pfad;
        this.schluessel = schluessel;
    }

    void beimStart(@Observes StartupEvent ereignis) {
        // Ohne Handbuch und ohne Schluessel laeuft die Anwendung weiter - sie antwortet dann eben nur aus der Datenbank.
        if (schluessel.isEmpty() || schluessel.get().isBlank()) {
            LOG.info("Kein OpenAI-Schluessel gesetzt - das Handbuch wird nicht aufgenommen.");
            return;
        }

        Path verzeichnis = Path.of(pfad);
        if (!Files.isDirectory(verzeichnis)) {
            LOG.infof("Kein Handbuch unter %s - der Assistent arbeitet ohne Handbuchwissen.",
                      verzeichnis.toAbsolutePath());
            return;
        }

        List<Document> dokumente = lade(verzeichnis);
        if (dokumente.isEmpty()) {
            LOG.infof("Keine .md-Dateien unter %s - der Assistent arbeitet ohne Handbuchwissen.",
                      verzeichnis.toAbsolutePath());
            return;
        }

        DocumentSplitter zerleger = DocumentSplitters.recursive(600, 100);

        // Die Aufnahme ruft OpenAI auf. Faellt das aus - kein Netz, falsches
        // Modell, kein Guthaben -, darf davon nicht der Start abhaengen.
        try {
            EmbeddingStoreIngestor.builder()
                                  .documentSplitter(zerleger)
                                  .embeddingModel(einbettung)
                                  .embeddingStore(speicher)
                                  .build()
                                  .ingest(dokumente);
            abschnitte = zerleger.splitAll(dokumente).size();
            LOG.infof("%d Abschnitte aus %d Handbuchdateien aufgenommen.", abschnitte, dokumente.size());
        } catch (RuntimeException fehler) {
            LOG.warnf("Das Handbuch liess sich nicht aufnehmen (%s) - der Assistent"
                      + " arbeitet ohne Handbuchwissen weiter.", fehler.getMessage());
        }
    }

    /** Wie viele Abschnitte im Vektorspeicher liegen. */
    public int aufgenommen() {
        return abschnitte;
    }

    private List<Document> lade(Path verzeichnis) {
        try (Stream<Path> dateien = Files.list(verzeichnis)) {
            return dateien.filter(datei -> datei.getFileName().toString().endsWith(".md"))
                          .sorted()
                          .map(datei -> FileSystemDocumentLoader.loadDocument(datei, new TextDocumentParser()))
                          .toList();
        } catch (IOException fehler) {
            LOG.info("Das Handbuch war nicht lesbar: " + fehler.getMessage());
            return List.of();
        }
    }
}
