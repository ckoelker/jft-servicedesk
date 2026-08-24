package de.netzfactor.servicedesk.dto;

/**
 * Entweder ein Wert oder ein Grund - der Ergebnistyp aus Block 6.
 *
 * <p>Eine Exception waere hier das falsche Mittel: sie beendet den Import bei
 * der ersten kaputten Zeile, obwohl die restlichen neun in Ordnung sind. Als
 * Wert laesst sich das Misslungene einsammeln, und am Ende steht beides im
 * Bericht - was uebernommen wurde und was warum nicht.
 *
 * @param <T> was im gelungenen Fall herauskommt
 */
public sealed interface Ergebnis<T> {

    /** Hat geklappt. */
    record Gelungen<T>(T wert) implements Ergebnis<T> {
    }

    /** Hat nicht geklappt - mit dem Grund, den man dem Anwender zeigen kann. */
    record Misslungen<T>(String grund) implements Ergebnis<T> {
    }

    static <T> Ergebnis<T> gelungen(T wert) {
        return new Gelungen<>(wert);
    }

    static <T> Ergebnis<T> misslungen(String grund) {
        return new Misslungen<>(grund);
    }
}
