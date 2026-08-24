package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;

/** Was gerade mit einem Ticket passiert ist - das, was ueber den Strom hinausgeht. */
public record Ereignis(String art, String kennung, String titel,
                       Prioritaet prioritaet, Status status) {

    public static Ereignis angelegt(Ticket ticket) {
        return von("angelegt", ticket);
    }

    public static Ereignis geaendert(Ticket ticket) {
        return von("geaendert", ticket);
    }

    public static Ereignis geloescht(Ticket ticket) {
        return von("geloescht", ticket);
    }

    private static Ereignis von(String art, Ticket ticket) {
        return new Ereignis(art, ticket.kennung, ticket.titel, ticket.prioritaet, ticket.status);
    }
}
