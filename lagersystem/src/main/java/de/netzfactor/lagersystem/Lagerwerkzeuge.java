package de.netzfactor.lagersystem;

import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Was das Lagersystem einem Sprachmodell anbietet.
 *
 * <p>Dieselben Daten wie unter /teile, nur anders verpackt: dort holt sie ein
 * Programm, hier ein Modell. Die Beschreibung an jedem Werkzeug ist kein
 * Kommentar, sondern die Schnittstelle - danach entscheidet das Modell, ob es
 * die Methode aufruft, und mit welchen Werten.
 *
 * <p>Die Wartezeit aus {@link LagerRessource} steht hier absichtlich nicht: sie
 * ist die Vorfuehrung zur Nebenlaeufigkeit und haette in einem Gespraech mit
 * dem Assistenten nur den Zweck, es langsam zu machen.
 */
@ApplicationScoped
public class Lagerwerkzeuge {

    private static final Logger LOG = LogManager.getLogger(Lagerwerkzeuge.class);

    private final Lager lager;

    public Lagerwerkzeuge(Lager lager) {
        this.lager = lager;
    }

    @Tool(description = """
            Schlaegt ein Ersatzteil im Lager nach und liefert Bezeichnung, Bestand und Lagerort.
            Teilenummern haben die Form T-1007.""")
    public String teilNachschlagen(
            @ToolArg(description = "Die Teilenummer, zum Beispiel T-1007") String nummer) {

        LOG.info("MCP teilNachschlagen({})", nummer);
        return lager.nach(nummer)
                    .map(Lagerwerkzeuge::beschreibe)
                    .orElse("Ein Teil mit der Nummer " + nummer + " gibt es im Lager nicht.");
    }

    @Tool(description = """
            Sucht Ersatzteile, deren Bezeichnung den Suchbegriff enthaelt, und liefert
            je Treffer Nummer, Bezeichnung, Bestand und Lagerort. Fuer Fragen wie
            "haben wir noch Netzteile" oder "welche Tastaturen liegen da".""")
    public String teileSuchen(
            @ToolArg(description = "Ein Wortteil der Bezeichnung, zum Beispiel Netzteil") String suchbegriff) {

        LOG.info("MCP teileSuchen({})", suchbegriff);
        if (suchbegriff == null || suchbegriff.isBlank()) {
            return "Ohne Suchbegriff kann ich nichts suchen.";
        }

        String gesucht = suchbegriff.strip().toLowerCase(Locale.GERMAN);
        List<Teil> treffer = lager.alle().stream()
                                  .filter(teil -> teil.bezeichnung().toLowerCase(Locale.GERMAN).contains(gesucht))
                                  .toList();

        if (treffer.isEmpty()) {
            return "Zu \"" + suchbegriff + "\" liegt nichts im Lager.";
        }
        return zeilen(treffer);
    }

    @Tool(description = """
            Liefert die Ersatzteile, deren Bestand unter einer Schwelle liegt - aufsteigend,
            das knappste zuerst. Fuer Fragen nach dem, was nachbestellt werden muss.""")
    public String knappeTeile(
            @ToolArg(description = "Bestandsschwelle, unterhalb derer ein Teil als knapp gilt",
                     required = false, defaultValue = "5") int schwelle) {

        LOG.info("MCP knappeTeile({})", schwelle);
        List<Teil> knapp = lager.alle().stream()
                                .filter(teil -> teil.bestand() < schwelle)
                                .sorted(Comparator.comparingInt(Teil::bestand))
                                .toList();

        if (knapp.isEmpty()) {
            return "Kein Teil liegt unter " + schwelle + " Stueck.";
        }
        return "Unter " + schwelle + " Stueck:\n" + zeilen(knapp);
    }

    private static String zeilen(List<Teil> teile) {
        return teile.stream().map(Lagerwerkzeuge::beschreibe).reduce((a, b) -> a + "\n" + b).orElse("");
    }

    private static String beschreibe(Teil teil) {
        return "%s %s: %d auf Lager (%s)".formatted(
                teil.nummer(), teil.bezeichnung(), teil.bestand(), teil.lagerort());
    }
}
