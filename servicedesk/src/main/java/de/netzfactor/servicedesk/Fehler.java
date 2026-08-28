package de.netzfactor.servicedesk;

import de.netzfactor.servicedesk.domain.Datenbank;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.dto.Ergebnis;
import de.netzfactor.servicedesk.dto.Meldung;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Block 6: was fehlen darf, steht in einem {@link Optional} - was schiefgehen
 * darf, in einem {@link Ergebnis}.
 */
public final class Fehler {

    private Fehler() {
    }

    public static void main(String[] args) {
        List<Ticket> tickets = Datenbank.tickets();

        System.out.println("Suche nach Kennung");
        System.out.println("------------------");
        for (String kennung : List.of("S-0007", "S-9999")) {
            String antwort = finde(tickets, kennung)
                    .map(ticket -> ticket.titel)
                    .orElse("nicht gefunden");
            System.out.printf("%-8s %s%n", kennung, antwort);
        }

        List<Ergebnis<Meldung>> ergebnisse = Meldungsimport.ausDemKlassenpfad("meldungen.csv");

        // Der Import laeuft bis zum Ende durch, weil eine kaputte Zeile ein Wert
        // ist und keine Exception - sonst gingen die guten Zeilen dahinter verloren.
        List<Meldung> uebernommen = new ArrayList<>();
        List<String> abgewiesen = new ArrayList<>();
        for (Ergebnis<Meldung> ergebnis : ergebnisse) {
            if (ergebnis instanceof Ergebnis.Gelungen<Meldung> gelungen) {
                uebernommen.add(gelungen.wert());
            } else if (ergebnis instanceof Ergebnis.Misslungen<Meldung> misslungen) {
                abgewiesen.add(misslungen.grund());
            }
        }

        System.out.println();
        System.out.println("Import aus meldungen.csv");
        System.out.println("------------------------");
        System.out.printf("%d übernommen, %d abgewiesen%n", uebernommen.size(), abgewiesen.size());
        abgewiesen.forEach(grund -> System.out.println("  " + grund));

        System.out.println();
        System.out.println("Übernommen");
        System.out.println("----------");
        System.out.printf("%-24s %-18s %-9s %s%n", "Firma", "Melder", "Priorität", "Titel");
        for (Meldung meldung : uebernommen) {
            System.out.printf("%-24s %-18s %-9s %s%n",
                    meldung.firma(), meldung.melder(), meldung.prioritaet(), meldung.titel());
        }
    }

    /** Kein Treffer ist hier ein normaler Ausgang, also Optional statt null. */
    static Optional<Ticket> finde(List<Ticket> alle, String kennung) {
        return alle.stream()
                .filter(ticket -> ticket.kennung.equals(kennung))
                .findFirst();
    }
}
