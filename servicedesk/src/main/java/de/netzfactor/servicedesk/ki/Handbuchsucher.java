package de.netzfactor.servicedesk.ki;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;

/** Haengt an jede Frage des Assistenten die passenden Handbuchstellen an - ohne dass der Assistent davon weiss. */
@ApplicationScoped
public class Handbuchsucher implements RetrievalAugmentor {

    private final RetrievalAugmentor sucher;

    public Handbuchsucher(EmbeddingStore<TextSegment> speicher, EmbeddingModel einbettung) {
        // Vier Treffer ab Aehnlichkeit 0.6: weniger Passendes im Kontext heisst weniger Gelegenheit zum Erfinden.
        this.sucher = DefaultRetrievalAugmentor
                .builder()
                .contentRetriever(EmbeddingStoreContentRetriever.builder()
                                                                .embeddingStore(speicher)
                                                                .embeddingModel(einbettung)
                                                                .maxResults(4)
                                                                .minScore(0.6)
                                                                .build())
                .build();
    }

    @Override
    public AugmentationResult augment(AugmentationRequest anfrage) {
        return sucher.augment(anfrage);
    }
}
