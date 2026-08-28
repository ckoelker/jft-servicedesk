package de.netzfactor.servicedesk.auswertung;

import de.netzfactor.servicedesk.domain.Datenbank;
import de.netzfactor.servicedesk.domain.Ticket;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Block 14: der Eskalationslauf - welche offenen Tickets haben die Zusage
 * bereits gerissen?
 *
 * <p>Dieselbe Pruefung laeuft hier dreimal: einmal mit einem ungeschuetzten
 * Zaehler, einmal mit einem {@link AtomicLong} und einmal ganz ohne Threads.
 * Nur eine der drei Zahlen darf abweichen - und genau die tut es.
 */
public final class Eskalation {

    private static final int LAEUFE = 200;

    private Eskalation() {
    }

    /** Ein Zaehler ohne jeden Schutz - so, wie man ihn versehentlich schreibt. */
    private static final class Zaehler {

        private long wert;

        void erhoehen() {
            // zaehler++ ist drei Schritte - lesen, addieren, schreiben. Zwei Threads
            // koennen denselben alten Wert lesen, und eine Erhoehung geht verloren.
            wert++;
        }
    }

    public static void main(String[] args) {
        List<Ticket> tickets = Datenbank.tickets();

        Kalender kalender = new Feiertage();
        LocalDateTime jetzt = LocalDateTime.now();
        List<Ticket> offene = tickets.stream().filter(ticket -> ticket.status.offen()).toList();

        long jeLauf = 0;
        for (Ticket ticket : offene) {
            if (!Sla.inDerZusage(ticket, kalender, jetzt)) {
                jeLauf++;
            }
        }
        long erwartet = jeLauf * LAEUFE;

        Zaehler ungeschuetzt = new Zaehler();
        AtomicLong sicher = new AtomicLong();

        List<Callable<Void>> aufgaben = offene.stream()
                .map(ticket -> (Callable<Void>) () -> {
                    if (!Sla.inDerZusage(ticket, kalender, jetzt)) {
                        ungeschuetzt.erhoehen();
                        sicher.incrementAndGet();
                    }
                    return null;
                })
                .toList();

        ExecutorService dienst = Executors.newFixedThreadPool(8);
        try {
            for (int lauf = 0; lauf < LAEUFE; lauf++) {
                dienst.invokeAll(aufgaben);
            }
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Der Eskalationslauf wurde unterbrochen", fehler);
        } finally {
            dienst.shutdown();
        }

        System.out.printf("Eskalationslauf: %d Durchläufe über %d offene Tickets%n",
                LAEUFE, offene.size());
        System.out.println("--------------------------------------------------");
        System.out.printf("%-14s %12d%n", "erwartet", erwartet);
        System.out.printf("%-14s %12d%n", "ungeschuetzt", ungeschuetzt.wert);
        System.out.printf("%-14s %12d%n", "AtomicLong", sicher.get());
        System.out.println();

        long fehlend = erwartet - ungeschuetzt.wert;
        if (fehlend == 0) {
            System.out.println("Der ungeschützte Zähler traf diesmal zufällig — das ist keine Zusage, "
                    + "sondern Glück.");
        } else {
            System.out.printf("Der ungeschützte Zähler lag um %d daneben: %d Erhöhungen sind verloren "
                    + "gegangen.%n", fehlend, fehlend);
        }

        long ausDemStream = offene.stream()
                .filter(ticket -> !Sla.inDerZusage(ticket, kalender, jetzt))
                .count();
        System.out.printf("Zum Vergleich, ohne Threads: stream().filter(...).count() = %d%n",
                ausDemStream);

        List<Ticket> ueberfaellig = offene.stream()
                .filter(ticket -> !Sla.inDerZusage(ticket, kalender, jetzt))
                .sorted(Comparator.comparing(
                        (Ticket ticket) -> Sla.faelligAm(ticket, kalender)))
                .limit(10)
                .toList();

        System.out.println();
        System.out.println("Die zehn am stärksten überfälligen Tickets");
        System.out.println("------------------------------------------");
        System.out.printf("%-8s %-40s %-10s %10s%n", "Kennung", "Titel", "Priorität", "Std. drüber");
        for (Ticket ticket : ueberfaellig) {
            Duration drueber = Duration.between(Sla.faelligAm(ticket, kalender), jetzt);
            System.out.printf("%-8s %-40s %-10s %10d%n",
                    ticket.kennung, ticket.titel, ticket.prioritaet, drueber.toHours());
        }
    }
}
