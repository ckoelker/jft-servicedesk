package de.netzfactor.servicedesk.auswertung;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Sagt, an welchen Tagen niemand arbeitet.
 *
 * <p>Ein Interface mit einer Methode - damit laesst sich die SLA-Rechnung im
 * Test mit einem Lambda versorgen, statt einen Feiertagsdienst zu starten.
 */
@FunctionalInterface
public interface Kalender {

    boolean arbeitsfrei(LocalDate tag);

    /** Der Notbehelf, wenn kein Feiertagsdienst erreichbar ist: nur das Wochenende. */
    static Kalender nurWochenende() {
        return tag -> tag.getDayOfWeek() == DayOfWeek.SATURDAY
                   || tag.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
