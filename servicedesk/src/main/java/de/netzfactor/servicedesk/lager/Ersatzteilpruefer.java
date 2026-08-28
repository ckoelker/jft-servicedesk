package de.netzfactor.servicedesk.lager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Fragt das Lagersystem, ob ein Ersatzteil da ist.
 *
 * <p>Im Quelltext steht kein einziges {@code new Lagerauskunft()} - gefuellt
 * wird das Feld von aussen. Genau deshalb laeuft dieselbe Klasse in Block 10 im
 * Mini-Container und ab Block 11 in CDI.
 */
@ApplicationScoped
public class Ersatzteilpruefer {

    // Zwei Annotationen, zwei Container: der Eigenbau aus Block 10 liest
    // @Braucht, Quarkus liest @Inject - an der Klasse aendert sich nichts.
    @Braucht
    @Inject
    Lagerauskunft lager;

    /** Ein Ausfall des Lagersystems ist hier eine Auskunft, keine Ausnahme. */
    public String pruefe(String nummer) {
        try {
            Teil teil = lager.nach(nummer);
            return "%s %s: %d auf Lager (%s)".formatted(
                    teil.nummer(), teil.bezeichnung(), teil.bestand(), teil.lagerort());
        } catch (RuntimeException fehler) {
            return "Zu " + nummer + " gibt das Lagersystem gerade keine Auskunft: "
                    + fehler.getMessage();
        }
    }
}
