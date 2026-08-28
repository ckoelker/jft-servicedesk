package de.netzfactor.servicedesk.ki;

import de.netzfactor.servicedesk.dto.Triage;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

/** Die Schnittstelle ist der ganze Dienst: Quarkus baut daraus beim Uebersetzen die Anbindung an das Modell. */
@RegisterAiService(
        // Jede Meldung wird fuer sich eingeordnet - ein Gedaechtnis wuerde hier nur alte Meldungen mitschleppen.
        chatMemoryProviderSupplier = RegisterAiService.NoChatMemoryProviderSupplier.class,
        // Das Handbuch gehoert zum Assistenten, nicht zur Triage.
        retrievalAugmentor = RegisterAiService.NoRetrievalAugmentorSupplier.class)
public interface Triagedienst {

    @SystemMessage("""
            Du arbeitest in der Annahme eines IT-ServiceDesk und ordnest eingehende Meldungen ein.
            Die Kategorie richtet sich danach, welches Betriebsmittel ausfällt - nicht danach,
            wer meldet oder wie dringend es ist. Passt nichts davon, nimmst du SONSTIGES.
            Der Titel ist eine sachliche Zeile mit höchstens acht Wörtern.
            Ein Rückruf ist nur bei kritischen Störungen nötig - also wenn mehrere Leute
            nicht arbeiten können oder ein Betrieb steht. Sonst niemals.
            Die Begründung ist ein einziger Satz.
            """)
    @UserMessage("Ordne diese Meldung ein: {meldung}")
    Triage einordnen(String meldung);

    @SystemMessage("""
            Du fasst den Verlauf von Tickets für die Übergabe an die nächste Schicht zusammen.
            Du schreibst sachlich, auf Deutsch, und erfindest nichts, was nicht im Verlauf steht.
            """)
    @UserMessage("Fasse den Verlauf dieses Tickets in hoechstens zwei Saetzen zusammen:\n{verlauf}")
    String zusammenfassen(String verlauf);
}
