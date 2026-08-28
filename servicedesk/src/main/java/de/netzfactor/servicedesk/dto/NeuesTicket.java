package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Was hereinkommt, wenn jemand ein Ticket meldet oder aendert.
 *
 * <p>Firma und Melder stehen als Werte am Ticket, deshalb kommen sie hier auch
 * als Werte herein - es gibt keine Stammdaten mehr, in denen man sie nachschlaegt.
 */
public record NeuesTicket(@NotBlank @Size(max = 255) String titel,
                          @Size(max = 2000) String beschreibung,
                          @NotNull Prioritaet prioritaet,
                          @NotNull Kategorie kategorie,
                          @NotBlank @Size(max = 255) String firma,
                          @NotBlank @Size(max = 255) String melder) {
}
