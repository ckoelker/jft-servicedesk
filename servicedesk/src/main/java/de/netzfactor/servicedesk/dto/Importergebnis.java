package de.netzfactor.servicedesk.dto;


import java.util.ArrayList;
import java.util.List;

/** Was der Import gebracht hat - beides steht drin, das Uebernommene und das Abgewiesene. */
public record Importergebnis(int uebernommen, int abgewiesen, List<String> gruende) {

    public static Importergebnis von(List<Ergebnis<Meldung>> ergebnisse) {
        List<String> gruende = new ArrayList<>();
        for (Ergebnis<Meldung> ergebnis : ergebnisse) {
            // Das Muster bindet den Grund gleich mit - ein Cast ist nicht noetig.
            if (ergebnis instanceof Ergebnis.Misslungen<Meldung> misslungen) {
                gruende.add(misslungen.grund());
            }
        }
        return new Importergebnis(ergebnisse.size() - gruende.size(), gruende.size(),
                                  List.copyOf(gruende));
    }
}
