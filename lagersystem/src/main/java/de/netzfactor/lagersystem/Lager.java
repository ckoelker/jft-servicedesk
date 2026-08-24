package de.netzfactor.lagersystem;

import jakarta.enterprise.context.ApplicationScoped;

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
    }

    public Optional<Teil> nach(String nummer) {
        return Optional.ofNullable(teile.get(nummer));
    }

    public List<Teil> alle() {
        return new ArrayList<>(teile.values());
    }
}
