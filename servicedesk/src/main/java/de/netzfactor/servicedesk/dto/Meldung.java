package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Prioritaet;

/** Eine gemeldete Stoerung, wie sie aus der CSV-Datei kommt - noch ohne Kennung. */
public record Meldung(String firma, String melder, String titel, Prioritaet prioritaet) {
}
