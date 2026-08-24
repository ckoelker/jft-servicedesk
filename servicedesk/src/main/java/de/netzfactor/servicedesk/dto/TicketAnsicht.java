package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;

import java.time.LocalDateTime;

/**
 * Ein Ticket, wie es nach draussen geht.
 *
 * <p>Die Entity selbst geht nicht hinaus, weil Jackson sonst an den lazy
 * Verweisen entlang die halbe Datenbank in die Antwort schreiben wuerde.
 */
public record TicketAnsicht(String kennung, String titel, String beschreibung,
                            Prioritaet prioritaet, Status status, Kategorie kategorie,
                            String firma, String melder, String bearbeiter,
                            LocalDateTime gemeldetAm, LocalDateTime erledigtAm) {

    public static TicketAnsicht von(Ticket ticket) {
        return new TicketAnsicht(ticket.kennung,
                                 ticket.titel,
                                 ticket.beschreibung,
                                 ticket.prioritaet,
                                 ticket.status,
                                 ticket.kategorie,
                                 ticket.firma,
                                 ticket.melder,
                                 ticket.bearbeiter,
                                 ticket.gemeldetAm,
                                 ticket.erledigtAm);
    }
}
