package de.netzfactor.servicedesk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.List;

/** Ein Eintrag im Verlauf eines Tickets. */
@Entity
public class Kommentar extends Basis {

    @Column(length = 2000)
    public String text;

    public String autor;
    public LocalDateTime geschriebenAm;

    // Nur diese Richtung: eine Rueckliste am Ticket brauchte niemand, waere aber
    // die erste Stelle, an der versehentlich alles nachgeladen wird.
    @ManyToOne(fetch = FetchType.LAZY)
    public Ticket ticket;

    public static List<Kommentar> zu(Ticket ticket) {
        return find("ticket = ?1 order by geschriebenAm", ticket).list();
    }
}
