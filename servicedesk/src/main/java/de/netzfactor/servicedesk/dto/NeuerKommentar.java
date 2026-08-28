package de.netzfactor.servicedesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Was hereinkommt, wenn jemand ein Ticket kommentiert. */
public record NeuerKommentar(@NotBlank @Size(max = 2000) String text,
                             @NotBlank String autor) {
}
