package de.netzfactor.servicedesk.auswertung;

import jakarta.inject.Singleton;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Der echte Feiertagsdienst: fragt date.nager.at nach den deutschen Feiertagen
 * eines Jahres und merkt sich die Antwort.
 *
 * <p>Damit bekommt die SLA-Rechnung dieselbe Schnittstelle wie im Test - nur
 * dass hinter dem Lambda diesmal ein fremder Server steht.
 *
 * <p>@Singleton und nicht @ApplicationScoped, weil die Klasse final bleiben
 * soll: fuer einen Singleton baut CDI keinen Stellvertreter.
 */
@Singleton
public final class Feiertage implements Kalender {

    private static final String BASIS = "https://date.nager.at/api/v3/PublicHolidays";
    private static final Kalender WOCHENENDE = Kalender.nurWochenende();

    // Gebraucht wird aus dem JSON nur "date":"2026-01-01" - fuer diesen einen
    // Zweck ist ein Muster ehrlicher als ein Objektmodell samt Jackson.
    private static final Pattern DATUM = Pattern.compile("\"date\"\\s*:\\s*\"(\\d{4}-\\d{2}-\\d{2})\"");

    private final String basis;
    private final HttpClient klient = HttpClient.newHttpClient();
    private final ConcurrentHashMap<Integer, Set<LocalDate>> jeJahr = new ConcurrentHashMap<>();

    public Feiertage() {
        this(BASIS);
    }

    public Feiertage(String basis) {
        this.basis = basis;
    }

    @Override
    public boolean arbeitsfrei(LocalDate tag) {
        return WOCHENENDE.arbeitsfrei(tag)
                || jeJahr.computeIfAbsent(tag.getYear(), this::hole).contains(tag);
    }

    /** Die Feiertage eines Jahres, so wie der Dienst sie liefert. */
    public Set<LocalDate> imJahr(int jahr) {
        return jeJahr.computeIfAbsent(jahr, this::hole);
    }

    private Set<LocalDate> hole(int jahr) {
        // Ein Ausfall darf hier nicht zum Abbruch fuehren: eine Auswertung ohne
        // Feiertage ist ungenau, eine Auswertung, die gar nicht laeuft, ist wertlos.
        try {
            HttpRequest anfrage = HttpRequest.newBuilder()
                    .uri(URI.create(basis + "/" + jahr + "/DE"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> antwort = klient.send(anfrage, HttpResponse.BodyHandlers.ofString());
            if (antwort.statusCode() != 200) {
                System.err.println("Feiertagsdienst antwortete mit Status " + antwort.statusCode()
                        + " - es wird nur das Wochenende gezählt.");
                return Set.of();
            }
            return lies(antwort.body());
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            System.err.println("Feiertagsdienst unterbrochen - es wird nur das Wochenende gezählt.");
            return Set.of();
        } catch (Exception fehler) {
            System.err.println("Feiertagsdienst nicht erreichbar (" + fehler.getMessage()
                    + ") - es wird nur das Wochenende gezählt.");
            return Set.of();
        }
    }

    private static Set<LocalDate> lies(String json) {
        Set<LocalDate> tage = new HashSet<>();
        Matcher treffer = DATUM.matcher(json);
        while (treffer.find()) {
            tage.add(LocalDate.parse(treffer.group(1)));
        }
        return Set.copyOf(tage);
    }

    public static void main(String[] args) {
        int jahr = Year.now().getValue();
        Feiertage feiertage = new Feiertage();
        List<LocalDate> tage = feiertage.imJahr(jahr).stream().sorted().toList();

        System.out.println("Feiertage " + jahr + ": " + tage.size());
        System.out.println("---------------------");
        tage.stream().limit(5).forEach(tag -> System.out.println("  " + tag + "  " + tag.getDayOfWeek()));

        if (tage.isEmpty()) {
            System.out.println("  (nichts geladen - die Auswertung läuft mit Kalender.nurWochenende())");
        }
    }
}
