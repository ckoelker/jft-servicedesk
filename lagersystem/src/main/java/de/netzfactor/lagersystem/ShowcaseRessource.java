package de.netzfactor.lagersystem;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ServiceUnavailableException;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Vier eingebaute Fehler zum Anschauen - jeder mit einem anderen Bild im
 * Profiler.
 *
 * <pre>
 *   GET    /showcase                        die Uebersicht
 *   GET    /showcase/leck?bloecke=50        Heap steigt und faellt nicht zurueck
 *   DELETE /showcase/leck                   Gegenprobe: der Speicher wird frei
 *   GET    /showcase/allokation?runden=20000  GC laeuft heiss, der Heap saegt
 *   GET    /showcase/suche?anzahl=20000     eine Methode frisst die ganze Zeit
 *   GET    /showcase/blockade?threads=32    Threads stehen, die CPU langweilt sich
 *   GET    /showcase/speicher               nur nachsehen, nichts anstellen
 * </pre>
 *
 * <p>Drei der vier gibt es in zwei Fassungen: {@code ?schnell=true} rechnet
 * dasselbe Ergebnis richtig. Der Vergleich der beiden Profile ist der
 * eigentliche Lerneffekt - ein Profiler sagt einem naemlich nicht "langsam",
 * sondern <em>wo</em>, und das sieht man erst mit einem Vorher und Nachher.
 *
 * <p>Die Anleitung dazu steht in {@code teilnehmerwuensche/PROFILING.md}.
 */
@Path("/showcase")
@Produces(MediaType.APPLICATION_JSON)
public class ShowcaseRessource {

    private static final Logger LOG = LogManager.getLogger(ShowcaseRessource.class);

    /** Obergrenzen, damit ein Tippfehler nicht den Rechner holt. */
    private static final int MAX_BLOECKE = 200;
    private static final int MAX_RUNDEN = 50_000;
    private static final int MAX_THREADS = 64;

    private final Fehlerspeicher fehlerspeicher;
    private final boolean aktiv;
    private final long verzoegerung;

    /** Ein Schloss fuer alle - der Engpass, den die Blockade vorfuehrt. */
    private final Object engpass = new Object();

    public ShowcaseRessource(Fehlerspeicher fehlerspeicher,
                             @ConfigProperty(name = "lagersystem.showcase.aktiv") boolean aktiv,
                             @ConfigProperty(name = "lagersystem.verzoegerung") long verzoegerung) {
        this.fehlerspeicher = fehlerspeicher;
        this.aktiv = aktiv;
        this.verzoegerung = verzoegerung;
    }

    /** Was die Antworten gemeinsam haben: was lief, wie lange, was kam heraus. */
    public record Ergebnis(String fall, String fassung, long dauerMs,
                           String ergebnis, long heapMb, long heapMaxMb) {
    }

    // ---------------------------------------------------------------- Uebersicht

    @GET
    public List<String> uebersicht() {
        return List.of(
                "GET    /showcase/leck?bloecke=50          Speicherleck",
                "DELETE /showcase/leck                     Gegenprobe",
                "GET    /showcase/allokation?runden=20000  Allokationsflut",
                "GET    /showcase/suche?anzahl=20000       CPU-Hotspot",
                "GET    /showcase/blockade?threads=32      Blockade",
                "GET    /showcase/speicher                 nur nachsehen",
                "",
                "Bei allokation, suche und blockade zeigt ?schnell=true die richtige Fassung.");
    }

    @GET
    @Path("/speicher")
    public Ergebnis speicher() {
        return baue("speicher", "-", 0,
                fehlerspeicher.eintraege() + " Bloecke im Zwischenspeicher");
    }

    // ---------------------------------------------------------------- 1 Speicherleck

    /**
     * Legt Megabyte in einem Zwischenspeicher ab, der nie geleert wird.
     *
     * <p>Im Diagramm unterscheidet sich das von gewoehnlichem Muell an einer
     * Stelle: Die <em>untere</em> Kante der Saegezahnkurve steigt mit. Was nach
     * einer vollen Sammlung uebrig bleibt, ist kein Muell - es ist ein Leck.
     */
    @GET
    @Path("/leck")
    public Ergebnis leck(@QueryParam("bloecke") @DefaultValue("50") int bloecke) {
        pruefe();
        int wieviel = begrenze(bloecke, MAX_BLOECKE);
        long start = System.nanoTime();
        int gesamt = fehlerspeicher.lege(wieviel);
        return baue("leck", "kaputt", start,
                wieviel + " MB abgelegt, jetzt " + gesamt + " MB");
    }

    /** Die Gegenprobe: danach faellt der Heap zurueck - es war wirklich das Leck. */
    @DELETE
    @Path("/leck")
    public Ergebnis aufraeumen() {
        pruefe();
        long start = System.nanoTime();
        int frei = fehlerspeicher.leere();
        return baue("leck", "aufgeraeumt", start, frei + " MB freigegeben");
    }

    // ---------------------------------------------------------------- 2 Allokationsflut

    /**
     * Baut eine Zeichenkette mit {@code +=} in der Schleife zusammen.
     *
     * <p>Jede Runde legt einen neuen String an und kopiert den alten hinein.
     * Das ist quadratisch im Aufwand <em>und</em> erzeugt Muellberge. Im
     * Profiler ist das der Fall, bei dem die GC-Zeit aus dem Ruder laeuft, ohne
     * dass eine einzelne eigene Methode auffaellt - die Arbeit steckt im
     * Kopieren, und das macht die Standardbibliothek.
     */
    @GET
    @Path("/allokation")
    public Ergebnis allokation(@QueryParam("runden") @DefaultValue("20000") int runden,
                               @QueryParam("schnell") boolean schnell) {
        pruefe();
        int wieviel = begrenze(runden, MAX_RUNDEN);
        LOG.info("Allokation: {} Runden, Fassung {}", wieviel, fassung(schnell));
        long start = System.nanoTime();

        int laenge;
        if (schnell) {
            StringBuilder bau = new StringBuilder();
            for (int i = 0; i < wieviel; i++) {
                bau.append("T-").append(1000 + i).append(';');
                melde(i);
            }
            laenge = bau.length();
        } else {
            String text = "";
            for (int i = 0; i < wieviel; i++) {
                text += "T-" + (1000 + i) + ";";   // der Fehler: kopiert jedes Mal alles
                melde(i);
            }
            laenge = text.length();
        }

        return baue("allokation", fassung(schnell), start, laenge + " Zeichen erzeugt");
    }

    // ---------------------------------------------------------------- 3 CPU-Hotspot

    /**
     * Sucht jede Nummer in einer Liste - mit {@code contains} in der Schleife.
     *
     * <p>Der Klassiker: {@code contains} laeuft auf einer Liste von vorn durch,
     * und das je Element einmal. Bei vierzig Teilen faellt das nicht auf, bei
     * zwanzigtausend steht ein Kern auf 100 Prozent. Der Sampler zeigt dann
     * eine einzige Methode als breiten Balken - deutlicher wird es nicht.
     */
    @GET
    @Path("/suche")
    public Ergebnis suche(@QueryParam("anzahl") @DefaultValue("20000") int anzahl,
                          @QueryParam("schnell") boolean schnell) {
        pruefe();
        int wieviel = begrenze(anzahl, MAX_RUNDEN);
        LOG.info("Suche: {} Nummern, Fassung {}", wieviel, fassung(schnell));
        long start = System.nanoTime();

        List<Integer> nummern = new ArrayList<>(wieviel);
        for (int i = 0; i < wieviel; i++) {
            nummern.add(1000 + i);
        }

        int treffer = 0;
        if (schnell) {
            // Dieselbe Frage an eine Datenstruktur, die sie beantworten kann.
            Set<Integer> schnellzugriff = new HashSet<>(nummern);
            for (int i = 0; i < wieviel; i++) {
                if (schnellzugriff.contains(1000 + i)) {
                    treffer++;
                }
            }
        } else {
            for (int i = 0; i < wieviel; i++) {
                if (nummern.contains(1000 + i)) {   // O(n) in einer O(n)-Schleife
                    treffer++;
                }
                melde(i);
            }
        }

        return baue("suche", fassung(schnell), start, treffer + " Treffer");
    }

    // ---------------------------------------------------------------- 4 Blockade

    /**
     * Schickt viele Threads durch einen Abschnitt, in den nur einer darf.
     *
     * <p>Das Gegenstueck zum Hotspot: Die Anwendung braucht ewig, aber die CPU
     * langweilt sich. In der Thread-Ansicht sieht man, warum - einer arbeitet,
     * alle anderen stehen auf BLOCKED. Ohne das Schloss laeuft dieselbe Arbeit
     * nebenlaeufig, und die Dauer faellt auf die einer einzigen Abfrage.
     */
    @GET
    @Path("/blockade")
    public Ergebnis blockade(@QueryParam("threads") @DefaultValue("32") int threads,
                             @QueryParam("schnell") boolean schnell) throws Exception {
        pruefe();
        int wieviel = begrenze(threads, MAX_THREADS);
        LOG.info("Blockade: {} Threads, Fassung {}", wieviel, fassung(schnell));
        long start = System.nanoTime();

        List<Callable<String>> auftraege = new ArrayList<>(wieviel);
        for (int i = 0; i < wieviel; i++) {
            int nummer = i;
            auftraege.add(() -> {
                if (schnell) {
                    return abfrage(nummer);              // jeder fuer sich
                }
                synchronized (engpass) {                 // alle durch dieselbe Tuer
                    return abfrage(nummer);
                }
            });
        }

        try (ExecutorService dienst = Executors.newVirtualThreadPerTaskExecutor()) {
            dienst.invokeAll(auftraege);
        }

        return baue("blockade", fassung(schnell), start, wieviel + " Abfragen erledigt");
    }

    /** Steht fuer den Zugriff auf ein langsames Fremdsystem. */
    private String abfrage(int nummer) throws InterruptedException {
        Thread.sleep(verzoegerung);
        LOG.debug("Abfrage {} fertig", nummer);
        return "T-" + (1000 + nummer);
    }

    // ---------------------------------------------------------------- Kleinkram

    private void pruefe() {
        if (!aktiv) {
            throw new ServiceUnavailableException(
                    "Der Showcase ist abgeschaltet. Einschalten mit "
                            + "-Dlagersystem.showcase.aktiv=true");
        }
    }

    private static String fassung(boolean schnell) {
        return schnell ? "richtig" : "kaputt";
    }

    private static int begrenze(int wert, int obergrenze) {
        return Math.max(1, Math.min(wert, obergrenze));
    }

    /** Alle tausend Runden eine Zeile - genug fuer die Rotation, zu wenig zum Stoeren. */
    private static void melde(int runde) {
        if (runde > 0 && runde % 1000 == 0) {
            LOG.debug("... {} Runden", runde);
        }
    }

    private Ergebnis baue(String fall, String fassung, long startNanos, String ergebnis) {
        long dauer = startNanos == 0 ? 0 : (System.nanoTime() - startNanos) / 1_000_000;
        Runtime laufzeit = Runtime.getRuntime();
        long belegtMb = (laufzeit.totalMemory() - laufzeit.freeMemory()) / (1024 * 1024);
        long maxMb = laufzeit.maxMemory() / (1024 * 1024);
        LOG.info("{} [{}] {} ms, {} - Heap {} von {} MB",
                fall, fassung, dauer, ergebnis, belegtMb, maxMb);
        return new Ergebnis(fall, fassung, dauer, ergebnis, belegtMb, maxMb);
    }
}
