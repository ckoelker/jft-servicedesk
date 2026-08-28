package de.netzfactor.servicedesk.ki;

import de.netzfactor.servicedesk.domain.Tokenkonto;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Der erste Waechter: hat der Benutzer sein Tagesbudget schon aufgebraucht?
 *
 * <p>Ein Guardrail ist eine gewoehnliche CDI-Bean. Er laeuft auf demselben
 * Thread wie die Anfrage - deshalb steht hier die {@link SecurityIdentity} zur
 * Verfuegung.
 *
 * <p><b>Er laeuft aber nicht ganz vorn.</b> Quarkus fuehrt erst den
 * RetrievalAugmentor aus - also die Handbuchsuche, und die ruft fuer die Frage
 * das Einbettungsmodell auf - und danach die Guardrails. Ein abgelehnter
 * Aufruf kostet deshalb nicht nichts, sondern eine Einbettung. Das ist wenig
 * (eine kurze Frage bei text-embedding-3-small), aber es ist nicht null, und
 * wer das Budget hart deckeln will, muss vor dem AiService pruefen statt darin.
 *
 * <p>Gebucht wird hier nichts. Das macht die {@link Tokenbuchhaltung},
 * nachdem das Modell geantwortet hat und der tatsaechliche Verbrauch feststeht.
 * Dieser Waechter zieht nur die Schranke herunter, sobald das Konto leer ist.
 */
@ApplicationScoped
public class Tokenwaechter implements InputGuardrail {

    private static final Logger LOG = Logger.getLogger(Tokenwaechter.class);

    @Inject
    SecurityIdentity identitaet;

    /**
     * Ein fester Wert fuer jeden - kein Tarifmodell, kein Feld an der Entity.
     * Fuer eine Vorfuehrung ist das die ehrlichste Form: man dreht die Zahl in
     * der application.properties klein und sieht die Sperre sofort.
     */
    @ConfigProperty(name = "servicedesk.assistent.tokens-pro-tag")
    int budget;

    @Override
    @Transactional
    public InputGuardrailResult validate(InputGuardrailRequest anfrage) {
        String benutzer = identitaet.getPrincipal().getName();
        int verbraucht = Tokenkonto.verbrauchtHeute(benutzer);

        if (verbraucht >= budget) {
            LOG.infof("Assistent fuer %s gesperrt: %d von %d Tokens verbraucht",
                      benutzer, verbraucht, budget);
            // fatal und nicht failure: nur fatal bricht die Kette ab. Ein
            // failure wird gesammelt, die uebrigen Waechter laufen trotzdem
            // weiter - und deren Meldung ueberschriebe dann diese hier.
            return fatal("Dein Tagesbudget von %d Tokens ist aufgebraucht (%d verbraucht)."
                                   .formatted(budget, verbraucht));
        }

        LOG.debugf("Assistent fuer %s zugelassen: %d von %d Tokens verbraucht",
                   benutzer, verbraucht, budget);
        return success();
    }
}
