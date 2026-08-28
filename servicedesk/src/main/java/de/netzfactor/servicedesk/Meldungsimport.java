package de.netzfactor.servicedesk;


import de.netzfactor.servicedesk.dto.Ergebnis;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.dto.Meldung;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Liest gemeldete Stoerungen aus einer CSV-Datei.
 *
 * <p>Jede Zeile wird fuer sich geprueft und zu einem {@link Ergebnis}. Eine
 * kaputte Zeile ist damit ein Wert in der Liste und kein Abbruch.
 */
public final class Meldungsimport {

    private Meldungsimport() {
    }

    public static List<Ergebnis<Meldung>> lies(BufferedReader quelle) {
        List<Ergebnis<Meldung>> ergebnisse = new ArrayList<>();
        try {
            quelle.readLine();
            int nummer = 1;
            String zeile;
            while ((zeile = quelle.readLine()) != null) {
                nummer++;
                if (!zeile.isBlank()) {
                    ergebnisse.add(pruefe(zeile, nummer));
                }
            }
        } catch (IOException fehler) {
            throw new UncheckedIOException("Datei liess sich nicht lesen", fehler);
        }
        return ergebnisse;
    }

    /** Der bequeme Weg fuer den Kurs: die Datei liegt neben den Klassen. */
    public static List<Ergebnis<Meldung>> ausDemKlassenpfad(String name) {
        InputStream strom = Meldungsimport.class.getClassLoader().getResourceAsStream(name);
        if (strom == null) {
            throw new IllegalArgumentException("Nicht im Klassenpfad: " + name);
        }
        try (BufferedReader quelle =
                     new BufferedReader(new InputStreamReader(strom, StandardCharsets.UTF_8))) {
            return lies(quelle);
        } catch (IOException fehler) {
            throw new UncheckedIOException("Datei liess sich nicht schliessen", fehler);
        }
    }

    /** Die erste Meldung, die durchgekommen ist - fuer den schnellen Blick. */
    public static Optional<Meldung> ersteGute(List<Ergebnis<Meldung>> ergebnisse) {
        for (Ergebnis<Meldung> ergebnis : ergebnisse) {
            if (ergebnis instanceof Ergebnis.Gelungen<Meldung> gelungen) {
                return Optional.of(gelungen.wert());
            }
        }
        return Optional.empty();
    }

    private static Ergebnis<Meldung> pruefe(String zeile, int nummer) {
        String[] felder = zeile.split(";", -1);
        if (felder.length != 4) {
            return Ergebnis.misslungen(
                    "Zeile %d: %d Felder statt 4".formatted(nummer, felder.length));
        }
        if (felder[2].isBlank()) {
            return Ergebnis.misslungen("Zeile %d: der Titel fehlt".formatted(nummer));
        }
        try {
            Prioritaet prioritaet = Prioritaet.valueOf(felder[3].strip());
            return Ergebnis.gelungen(new Meldung(felder[0].strip(), felder[1].strip(),
                    felder[2].strip(), prioritaet));
        } catch (IllegalArgumentException fehler) {
            // valueOf wirft bei unbekanntem Namen - hier wird daraus wieder ein Wert.
            return Ergebnis.misslungen(
                    "Zeile %d: unbekannte Priorität '%s'".formatted(nummer, felder[3].strip()));
        }
    }
}
