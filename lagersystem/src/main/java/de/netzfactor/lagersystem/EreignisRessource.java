package de.netzfactor.lagersystem;

import io.reactivex.rxjava3.core.Observable;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Die Zusatzfunktion der Lagerverwaltung - und die einzige Stelle im Projekt,
 * an der RxJava vorkommt. Der ServiceDesk ruft sie nie auf.
 *
 * <p>Der Fall: An einem Teil wird mehrfach kurz hintereinander gebucht. Daraus
 * soll <b>eine</b> Nachbestellwarnung werden, nicht fuenf. Das ist Rechnen
 * ueber die Zeit - und dafuer hat die Standardbibliothek bis heute nichts.
 *
 * <pre>
 *   GET /ereignisse              mit RxJava: groupBy + debounce
 *   GET /ereignisse?fassung=hand dasselbe von Hand, zum Vergleich
 * </pre>
 *
 * <p>Warum dafuer RxJava und warum virtuelle Threads es sonst ueberall
 * abgeloest haben, steht in {@code teilnehmerwuensche/RXJAVA.md}.
 */
@Path("/ereignisse")
@Produces(MediaType.APPLICATION_JSON)
public class EreignisRessource {

    /** Wie lange Ruhe sein muss, bevor eine Warnung herausgeht. */
    private static final long RUHE_MS = 300;

    private record Buchung(String teil, int menge, long pauseDavorMs) {
    }

    /** Feste Abstaende, damit jeder Lauf im Kurs dasselbe Bild ergibt. */
    private static final List<Buchung> BUCHUNGEN = List.of(
            new Buchung("T-1001", 3,   0),
            new Buchung("T-1001", 2,  80),
            new Buchung("T-1001", 5,  80),   // drei Schuebe, eine Warnung
            new Buchung("T-1007", 1, 500),
            new Buchung("T-1001", 1, 500),   // nach der Ruhe: neue Warnung
            new Buchung("T-1007", 4, 500));

    @GET
    public List<String> warnungen(@QueryParam("fassung") String fassung) {
        long start = System.nanoTime();
        List<String> ausgabe = new ArrayList<>();

        // Die Quelle: Buchungen treffen mit Abstand ein, nicht alle auf einmal.
        Observable<Buchung> eingang = Observable.fromIterable(BUCHUNGEN)
                .concatMap(b -> Observable.just(b)
                        .delay(b.pauseDavorMs(), TimeUnit.MILLISECONDS));

        if ("hand".equals(fassung)) {
            vonHand(eingang, ausgabe, start);
        } else {
            eingang
                    // Aus einem Strom werden viele: einer je Teil ...
                    .groupBy(Buchung::teil)
                    // ... und in jedem zaehlt nur, was nach 300 ms Ruhe uebrig ist.
                    .flatMap(jeTeil -> jeTeil.debounce(RUHE_MS, TimeUnit.MILLISECONDS))
                    .blockingSubscribe(b -> ausgabe.add(zeile(start, b, "rx")));
        }

        ausgabe.add("%d Buchungen hinein, %d Warnungen heraus."
                .formatted(BUCHUNGEN.size(), ausgabe.size()));
        return ausgabe;
    }

    /**
     * Dasselbe ohne die beiden Operatoren: je Teil ein Eintrag, je Eintrag ein
     * Zeitpunkt, und am Ende von Hand entscheiden, was durchgeht. Es tut, was
     * es soll - aber der Zustand steht jetzt hier statt in zwei Zeilen.
     */
    private void vonHand(Observable<Buchung> eingang, List<String> ausgabe, long start) {
        Map<String, Buchung> letzte = new LinkedHashMap<>();
        Map<String, Long> zeitpunkt = new LinkedHashMap<>();

        eingang.blockingSubscribe(b -> {
            long jetzt = System.nanoTime();
            Long vorher = zeitpunkt.get(b.teil());
            // War lange genug Ruhe, geht die vorherige Buchung als Warnung raus.
            if (vorher != null && (jetzt - vorher) / 1_000_000 >= RUHE_MS) {
                ausgabe.add(zeile(start, letzte.get(b.teil()), "hand"));
            }
            letzte.put(b.teil(), b);
            zeitpunkt.put(b.teil(), jetzt);
        });

        // Und was am Ende offen ist, muss man auch noch selbst nachraeumen.
        letzte.values().forEach(b -> ausgabe.add(zeile(start, b, "hand")));
    }

    private static String zeile(long start, Buchung b, String woher) {
        return "nach %4d ms  [%s]  %s: Nachbestellung pruefen (zuletzt %d Stueck)"
                .formatted((System.nanoTime() - start) / 1_000_000,
                        woher, b.teil(), b.menge());
    }
}
