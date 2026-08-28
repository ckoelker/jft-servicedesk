package de.netzfactor.servicedesk.auswertung;

import de.netzfactor.servicedesk.domain.Datenbank;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.domain.Zeitbuchung;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Block 9: dieselben drei Berichte aus Block 8, jetzt als Text, als Excel und
 * als PDF.
 *
 * <p>Die Berichte aendern sich nicht - nur der Schreiber kommt dazu.
 */
public final class Export {

    private static final Path ZIEL = Path.of("ziel");

    private Export() {
    }

    /** Ein fertiger Bericht: Ueberschrift und Zeilen, ohne Wissen ueber das Format. */
    private record Bericht(String titel, List<?> daten) {
    }

    public static void main(String[] args) throws IOException {
        List<Ticket> tickets = Datenbank.tickets();
        List<Zeitbuchung> buchungen = Datenbank.zeitbuchungen();

        // Derselbe Feiertagsdienst wie in der Anwendung - sonst weichen die
        // Zahlen im Bericht von denen im Browser ab.
        Kalender kalender = new Feiertage();
        List<Bericht> berichte = List.of(
                new Bericht("SLA-Quote", Berichte.slaQuote(tickets, kalender)),
                new Bericht("Top-Melder", Berichte.topMelder(tickets)),
                new Bericht("Auslastung", Berichte.auslastung(tickets, buchungen)));

        for (Bericht bericht : berichte) {
            System.out.println(Berichtsschreiber.alsText(bericht.titel(), bericht.daten()));
            System.out.println();
        }

        Files.createDirectories(ZIEL);
        System.out.println("Geschrieben");
        System.out.println("-----------");
        for (Bericht bericht : berichte) {
            String name = bericht.titel().toLowerCase(Locale.GERMANY);
            lege(ZIEL.resolve(name + ".xlsx"),
                 Berichtsschreiber.alsExcel(bericht.titel(), bericht.daten()));
            lege(ZIEL.resolve(name + ".pdf"),
                 Berichtsschreiber.alsPdf(bericht.titel(), bericht.daten()));
        }
    }

    private static void lege(Path datei, byte[] inhalt) throws IOException {
        Files.write(datei, inhalt);
        System.out.printf(Locale.GERMANY, "%s  (%.1f KB)%n",
                datei.toAbsolutePath(), inhalt.length / 1024.0);
    }
}
