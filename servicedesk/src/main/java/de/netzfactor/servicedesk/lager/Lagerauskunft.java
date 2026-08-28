package de.netzfactor.servicedesk.lager;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Auskunft des Lagersystems - ein blockierender Aufruf je Teil.
 *
 * <p>Die Klasse ist absichtlich weder final noch statisch: so laesst sie sich
 * im Test durch ein Mockito-Mock ersetzen, ohne dass am Quelltext etwas
 * geaendert werden muss.
 */
@ApplicationScoped
public class Lagerauskunft {

    private static final String BASIS = "http://localhost:8082";

    // Vier Felder aus einem flachen JSON - fuer diesen einen Zweck ist ein
    // Muster ehrlicher als ein Objektmodell samt Abhaengigkeit.
    private static final Pattern NUMMER = Pattern.compile("\"nummer\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern BEZEICHNUNG = Pattern.compile("\"bezeichnung\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern BESTAND = Pattern.compile("\"bestand\"\\s*:\\s*(-?\\d+)");
    private static final Pattern LAGERORT = Pattern.compile("\"lagerort\"\\s*:\\s*\"([^\"]*)\"");

    private final String basis;
    private final HttpClient klient = HttpClient.newHttpClient();

    public Lagerauskunft() {
        this(BASIS);
    }

    public Lagerauskunft(String basis) {
        this.basis = basis;
    }

    public Teil nach(String nummer) {
        try {
            HttpRequest anfrage = HttpRequest.newBuilder()
                    .uri(URI.create(basis + "/teile/" + nummer))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> antwort = klient.send(anfrage, HttpResponse.BodyHandlers.ofString());
            if (antwort.statusCode() != 200) {
                throw new IllegalStateException(
                        "Lagersystem meldet für " + nummer + " den Status " + antwort.statusCode());
            }
            String json = antwort.body();
            return new Teil(
                    wert(json, NUMMER, "nummer"),
                    wert(json, BEZEICHNUNG, "bezeichnung"),
                    Integer.parseInt(wert(json, BESTAND, "bestand")),
                    wert(json, LAGERORT, "lagerort"));
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Abruf von " + nummer + " wurde unterbrochen", fehler);
        } catch (IOException fehler) {
            throw new IllegalStateException("Lagersystem nicht erreichbar: " + basis, fehler);
        }
    }

    private static String wert(String json, Pattern muster, String feld) {
        Matcher treffer = muster.matcher(json);
        if (!treffer.find()) {
            throw new IllegalStateException("Feld " + feld + " fehlt in der Antwort: " + json);
        }
        return treffer.group(1);
    }
}
