package de.netzfactor.servicedesk.auswertung;

/**
 * Die drei Auswertungszeilen aus Block 8.
 *
 * <p>In Block 8 sind das nackte records. In Block 9 kommt an jede Komponente
 * eine {@link Spalte} - und derselbe Bericht faellt danach als Text, als Excel
 * und als PDF heraus, ohne dass hier noch etwas dazukommt.
 */
public final class Zeilen {

    private Zeilen() {
    }

    /** Halten wir die Zusage? Je Kategorie. */
    public record Sla(@Spalte("Kategorie") String kategorie,
                      @Spalte("Erledigt") long erledigt,
                      @Spalte("In der Zusage") long inDerZusage,
                      @Spalte("Quote in %") double quote) {
    }

    /** Wer meldet am meisten? */
    public record Melder(@Spalte("Firma") String firma,
                         @Spalte("Melder") String melder,
                         @Spalte("Tickets") long tickets,
                         @Spalte("davon kritisch") long kritisch) {
    }

    /** Wer arbeitet wie viel? */
    public record Auslastung(@Spalte("Bearbeiter") String bearbeiter,
                             @Spalte("Tickets") long tickets,
                             @Spalte("Stunden") double stunden) {
    }
}
