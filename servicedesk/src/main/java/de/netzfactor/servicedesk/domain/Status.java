package de.netzfactor.servicedesk.domain;

/** Wo ein Ticket gerade steht. */
public enum Status {

    NEU,
    IN_ARBEIT,
    WARTET,
    ERLEDIGT;

    /** Alles ausser ERLEDIGT zaehlt zur Warteschlange. */
    public boolean offen() {
        return this != ERLEDIGT;
    }
}
