package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Kommentar;

import java.time.LocalDateTime;

/** Ein Kommentar, wie er nach draussen geht - ohne den Rueckverweis auf das Ticket. */
public record KommentarAnsicht(String autor, String text, LocalDateTime geschriebenAm) {

    public static KommentarAnsicht von(Kommentar kommentar) {
        return new KommentarAnsicht(kommentar.autor, kommentar.text, kommentar.geschriebenAm);
    }
}
