package de.netzfactor.servicedesk.domain;

import java.time.LocalDateTime;

/** Ein Eintrag im Verlauf eines Tickets. */
public class Kommentar {

    public String text;
    public String autor;
    public LocalDateTime geschriebenAm;

    // Nur diese Richtung: eine Rueckliste am Ticket brauchte niemand, waere aber
    // die erste Stelle, an der versehentlich alles nachgeladen wird.
    public Ticket ticket;
}
