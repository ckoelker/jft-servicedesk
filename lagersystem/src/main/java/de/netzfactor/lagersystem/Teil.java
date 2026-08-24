package de.netzfactor.lagersystem;

/** Ein Ersatzteil, so wie das Lagersystem es nach aussen gibt. */
public record Teil(String nummer, String bezeichnung, int bestand, String lagerort) {
}
