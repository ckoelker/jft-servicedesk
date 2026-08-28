package de.netzfactor.servicedesk.ki;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Locale;

/**
 * Der zweite Waechter: geht es fachlich ueberhaupt um IT-Tickets?
 *
 * <p>Er steht hinter dem {@link Tokenwaechter}, weil er selbst einen Aufruf
 * kostet. Wessen Budget leer ist, bezahlt auch keine Themenpruefung mehr.
 *
 * <p>Faellt der {@link Themenpruefer} aus, laesst dieser Waechter die Frage
 * durch. Ein Filter, der bei einer Stoerung die ganze Anwendung dichtmacht,
 * waere schlimmer als der Fall, den er verhindern soll.
 */
@ApplicationScoped
public class Themenwaechter implements InputGuardrail {

    private static final Logger LOG = Logger.getLogger(Themenwaechter.class);

    @Inject
    Themenpruefer pruefer;

    @Override
    public InputGuardrailResult validate(InputGuardrailRequest anfrage) {
        String eingabe = anfrage.userMessage().singleText();

        String urteil;
        try {
            urteil = pruefer.gehoertDazu(eingabe);
        } catch (RuntimeException fehler) {
            LOG.warn("Themenpruefung nicht moeglich, Frage wird durchgelassen", fehler);
            return success();
        }

        // Das Modell soll ein Wort liefern, haelt sich aber nicht immer daran -
        // deshalb wird auf NEIN geprueft und nicht auf Gleichheit mit JA.
        if (urteil != null && urteil.strip().toUpperCase(Locale.GERMAN).startsWith("NEIN")) {
            LOG.infof("Frage abgelehnt, kein IT-Thema: %s", eingabe);
            return fatal("""
                    Das hat nichts mit dem IT-Servicedesk zu tun. Frag mich nach Tickets, \
                    Stoerungen, Ersatzteilen oder dem Handbuch.""");
        }

        return success();
    }
}
