package de.netzfactor.servicedesk.dto;

import java.util.List;

/** Das Ergebnis eines Eskalationslaufs - wie viel geprueft wurde und was gerissen ist. */
public record Eskalationslauf(int geprueft, int ueberfaellig, List<String> kennungen) {
}
