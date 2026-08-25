package de.netzfactor.lagersystem;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Der Lagerbestand - vierzig Teile, beim Start einmal aufgebaut.
 *
 * <p>Alle Werte werden aus der Nummer gerechnet, damit jeder Start dieselben
 * Daten liefert und ein Ergebnis im Kurs nachvollziehbar bleibt.
 */
@ApplicationScoped
public class Lager {

    // Die Log4j-API. Der Adapter im pom.xml leitet sie an den LogManager von
    // Quarkus weiter - deshalb gibt es hier keine log4j2.xml.
    private static final Logger LOG = LogManager.getLogger(Lager.class);

    private static final List<String> BEZEICHNUNGEN = List.of(
            "Tonerkassette",
            "Netzteil 65 W",
            "SFP-Modul",
            "Tastatur DE",
            "Dockingstation",
            "Speichermodul 16 GB",
            "Luefter",
            "Netzwerkkabel 3 m",
            "Festplatte 1 TB",
            "Monitorkabel HDMI");

    private final Map<String, Teil> teile = new LinkedHashMap<>();

    public Lager() {
        for (int n = 1; n <= 40; n++) {
            String nummer = "T-" + (1000 + n);
            String bezeichnung = BEZEICHNUNGEN.get((n - 1) % BEZEICHNUNGEN.size())
                    + " " + ((n - 1) / BEZEICHNUNGEN.size() + 1);
            teile.put(nummer, new Teil(nummer, bezeichnung, (n * 7) % 25, "Regal " + ((n % 8) + 1)));
        }
        LOG.info("Lagerbestand aufgebaut: {} Teile", teile.size());
    }

    public Optional<Teil> nach(String nummer) {
        Optional<Teil> gefunden = Optional.ofNullable(teile.get(nummer));
        // Ein unbekanntes Teil ist keine Stoerung des Lagers, sondern eine
        // Frage nach etwas, das es nicht gibt - deshalb WARN und nicht ERROR.
        if (gefunden.isEmpty()) {
            LOG.warn("Teil {} ist im Bestand nicht vorhanden", nummer);
        } else {
            LOG.debug("Teil {} gefunden: {}", nummer, gefunden.get().bezeichnung());
        }
        return gefunden;
    }

    public List<Teil> alle() {
        LOG.debug("Vollstaendiger Bestand abgefragt: {} Teile", teile.size());
        return new ArrayList<>(teile.values());
    }
}
