package de.netzfactor.servicedesk.domain;

import java.time.Duration;

/**
 * Wie dringend ein Ticket ist.
 *
 * <p>Der Faktor verkuerzt die Zusage aus der Kategorie: dieselbe Kategorie mit
 * 24 Stunden Zusage bedeutet bei KRITISCH sechs Stunden. So steht die Regel an
 * einer Stelle statt in jeder Auswertung.
 */
public enum Prioritaet {

    NIEDRIG(2.0),
    NORMAL(1.0),
    HOCH(0.5),
    KRITISCH(0.25);

    private final double faktor;

    Prioritaet(double faktor) {
        this.faktor = faktor;
    }

    /** Wie lange die Bearbeitung dauern darf, wenn die Kategorie so viel zusagt. */
    public Duration frist(int slaStunden) {
        return Duration.ofMinutes(Math.round(slaStunden * 60 * faktor));
    }
}
