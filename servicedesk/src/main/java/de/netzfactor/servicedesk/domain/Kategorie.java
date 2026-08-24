package de.netzfactor.servicedesk.domain;

/**
 * Worum es geht - und wie schnell es gehen muss.
 *
 * <p>Eine eigene Stammdatentabelle waere fachlich richtig, aber die sechs
 * Kategorien aendern sich nie: als enum stehen Bezeichnung und Zusage direkt am
 * Wert, und die Datenbank kommt mit einer Spalte statt mit einem Join aus.
 */
public enum Kategorie {

    ZUGANG("Zugang und Passwort", 4),
    NETZ("Netzwerk und VPN", 8),
    DRUCKER("Drucker und Scanner", 24),
    HARDWARE("Hardware und Geräte", 24),
    SOFTWARE("Software und Updates", 48),
    SONSTIGES("Sonstiges", 72);

    private final String bezeichnung;
    private final int slaStunden;

    Kategorie(String bezeichnung, int slaStunden) {
        this.bezeichnung = bezeichnung;
        this.slaStunden = slaStunden;
    }

    /** Was auf der Seite steht. */
    public String bezeichnung() {
        return bezeichnung;
    }

    /** Was dem Kunden zugesagt ist, bevor die Prioritaet die Frist verkuerzt. */
    public int slaStunden() {
        return slaStunden;
    }

    /** Unbekanntes faellt auf SONSTIGES zurueck, statt den Aufrufer scheitern zu lassen. */
    public static Kategorie nach(String name) {
        for (Kategorie kategorie : values()) {
            if (kategorie.name().equalsIgnoreCase(name)) {
                return kategorie;
            }
        }
        return SONSTIGES;
    }
}
