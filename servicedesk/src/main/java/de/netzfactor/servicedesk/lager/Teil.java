package de.netzfactor.servicedesk.lager;

/** Ein Ersatzteil, so wie das Lagersystem es liefert. */
public record Teil(String nummer, String bezeichnung, int bestand, String lagerort) {
}
