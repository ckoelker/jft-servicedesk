package de.netzfactor.servicedesk.ki;

import de.netzfactor.servicedesk.domain.Tokenkonto;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Schreibt mit, was jeder Aufruf beim Modell gekostet hat.
 *
 * <p>Eine CDI-Bean vom Typ {@link ChatModelListener} zieht die Erweiterung von
 * selbst heran - es gibt keine Stelle, an der sie angemeldet wird. Sie haengt
 * am Modell, nicht am Assistenten: deshalb zaehlt auch der Aufruf des
 * {@link Themenpruefer} mit, und deshalb faellt keine Werkzeugrunde durchs
 * Raster. Ein Gespraech mit drei Werkzeugaufrufen bucht viermal.
 *
 * <p>{@code onResponse} laeuft im selben Aufruf wie die Anfrage, also auf dem
 * Thread der HTTP-Anfrage. Nur deshalb steht hier die {@link SecurityIdentity}
 * noch zur Verfuegung.
 */
@ApplicationScoped
public class Tokenbuchhaltung implements ChatModelListener {

    private static final Logger LOG = Logger.getLogger(Tokenbuchhaltung.class);

    @Inject
    SecurityIdentity identitaet;

    @Override
    public void onResponse(ChatModelResponseContext antwort) {
        TokenUsage verbrauch = antwort.chatResponse().tokenUsage();
        if (verbrauch == null || verbrauch.totalTokenCount() == null) {
            // Nicht jedes Modell meldet den Verbrauch. Dann wird nicht geschaetzt,
            // sondern nichts gebucht - eine erfundene Zahl waere schlimmer als keine.
            return;
        }

        String benutzer = benutzername();
        if (benutzer == null) {
            LOG.debug("Modellaufruf ohne angemeldeten Benutzer - nichts gebucht");
            return;
        }

        buche(benutzer, verbrauch.totalTokenCount());
    }

    /**
     * Eine eigene Transaktion: die Buchung soll auch dann stehen bleiben, wenn
     * der Aufruf danach noch scheitert. Verbraucht ist verbraucht.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    void buche(String benutzer, int tokens) {
        Tokenkonto konto = Tokenkonto.heute(benutzer);
        konto.verbraucht += tokens;
        LOG.infof("%s: %d Tokens gebucht, heute insgesamt %d", benutzer, tokens, konto.verbraucht);
    }

    /** Ohne Anmeldung - etwa beim Aufnehmen des Handbuchs beim Start - gibt es niemanden zu belasten. */
    private String benutzername() {
        if (identitaet == null || identitaet.isAnonymous() || identitaet.getPrincipal() == null) {
            return null;
        }
        return identitaet.getPrincipal().getName();
    }
}
