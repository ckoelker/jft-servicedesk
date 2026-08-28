package de.netzfactor.servicedesk.auswertung;

import de.netzfactor.servicedesk.domain.Datenbank;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.domain.Zeitbuchung;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Block 8: drei Auswertungen, je ein paar Zeilen.
 *
 * <p>Diese drei Methoden sind der Kern des Projekts: Block 9 schreibt ihr
 * Ergebnis nach Excel und PDF, Block 14 zeigt es im Browser. Beide rufen genau
 * das hier auf und rechnen nichts nach.
 */
public final class Berichte {

    private Berichte() {
    }

    /** Halten wir die Zusage? Gezaehlt wird nur, was schon erledigt ist. */
    public static List<Zeilen.Sla> slaQuote(List<Ticket> tickets, Kalender kalender) {
        LocalDateTime jetzt = LocalDateTime.now();
        return tickets.stream()
                .filter(ticket -> ticket.status == Status.ERLEDIGT)
                .collect(Collectors.groupingBy(ticket -> ticket.kategorie))
                .entrySet().stream()
                .map(eintrag -> {
                    long erledigt = eintrag.getValue().size();
                    long puenktlich = eintrag.getValue().stream()
                            .filter(ticket -> Sla.inDerZusage(ticket, kalender, jetzt))
                            .count();
                    return new Zeilen.Sla(eintrag.getKey().bezeichnung(), erledigt, puenktlich,
                            prozent(puenktlich, erledigt));
                })
                .sorted(Comparator.comparingDouble(Zeilen.Sla::quote).reversed())
                .toList();
    }

    /** Wer meldet am meisten - und wie oft ist es wirklich kritisch? */
    public static List<Zeilen.Melder> topMelder(List<Ticket> tickets) {
        return tickets.stream()
                .collect(Collectors.groupingBy(ticket -> new Wer(ticket.firma, ticket.melder)))
                .entrySet().stream()
                .map(eintrag -> new Zeilen.Melder(
                        eintrag.getKey().firma(),
                        eintrag.getKey().melder(),
                        eintrag.getValue().size(),
                        eintrag.getValue().stream()
                                .filter(ticket -> ticket.prioritaet == Prioritaet.KRITISCH)
                                .count()))
                .sorted(Comparator.comparingLong(Zeilen.Melder::tickets).reversed())
                .limit(8)
                .toList();
    }

    /** Wer arbeitet wie viel - Tickets aus der Zuordnung, Stunden aus den Buchungen. */
    public static List<Zeilen.Auslastung> auslastung(List<Ticket> tickets,
                                                     List<Zeitbuchung> buchungen) {
        Map<String, Integer> minuten = buchungen.stream()
                .collect(Collectors.groupingBy(buchung -> buchung.bearbeiter,
                        Collectors.summingInt(buchung -> buchung.minuten)));
        return tickets.stream()
                .filter(ticket -> ticket.bearbeiter != null)
                .collect(Collectors.groupingBy(ticket -> ticket.bearbeiter, Collectors.counting()))
                .entrySet().stream()
                .map(eintrag -> new Zeilen.Auslastung(eintrag.getKey(), eintrag.getValue(),
                        stunden(minuten.getOrDefault(eintrag.getKey(), 0))))
                .sorted(Comparator.comparingDouble(Zeilen.Auslastung::stunden).reversed())
                .toList();
    }

    public static void main(String[] args) {
        List<Ticket> tickets = Datenbank.tickets();
        List<Zeitbuchung> buchungen = Datenbank.zeitbuchungen();

        System.out.println("SLA-Quote je Kategorie");
        System.out.println("----------------------");
        System.out.printf("%-24s %9s %14s %10s%n",
                "Kategorie", "Erledigt", "In der Zusage", "Quote in %");
        for (Zeilen.Sla zeile : slaQuote(tickets, new Feiertage())) {
            System.out.printf("%-24s %9d %14d %10.1f%n",
                    zeile.kategorie(), zeile.erledigt(), zeile.inDerZusage(), zeile.quote());
        }

        System.out.println();
        System.out.println("Die häufigsten Melder");
        System.out.println("---------------------");
        System.out.printf("%-24s %-18s %8s %15s%n",
                "Firma", "Melder", "Tickets", "davon kritisch");
        for (Zeilen.Melder zeile : topMelder(tickets)) {
            System.out.printf("%-24s %-18s %8d %15d%n",
                    zeile.firma(), zeile.melder(), zeile.tickets(), zeile.kritisch());
        }

        System.out.println();
        System.out.println("Auslastung der Bearbeiter");
        System.out.println("-------------------------");
        System.out.printf("%-20s %8s %9s%n", "Bearbeiter", "Tickets", "Stunden");
        for (Zeilen.Auslastung zeile : auslastung(tickets, buchungen)) {
            System.out.printf("%-20s %8d %9.1f%n",
                    zeile.bearbeiter(), zeile.tickets(), zeile.stunden());
        }
    }

    /** Firma und Melder zusammen als Schluessel - der record vergleicht seine Werte selbst. */
    private record Wer(String firma, String melder) {
    }

    private static double prozent(long teil, long ganz) {
        return ganz == 0 ? 0.0 : Math.round(teil * 1000.0 / ganz) / 10.0;
    }

    private static double stunden(int minuten) {
        return Math.round(minuten / 6.0) / 10.0;
    }
}
