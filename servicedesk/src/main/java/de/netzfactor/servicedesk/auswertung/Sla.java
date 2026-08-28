package de.netzfactor.servicedesk.auswertung;

import de.netzfactor.servicedesk.domain.Ticket;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Die eine Regel, an der alles haengt: Wann muss ein Ticket fertig sein?
 *
 * <p>Grundlage ist die Zusage der Kategorie, verkuerzt um die Prioritaet.
 * Arbeitsfreie Tage zwischen Meldung und Frist zaehlen nicht mit - sonst reisst
 * jedes Ticket vom Freitagnachmittag die Zusage.
 */
public final class Sla {

    private Sla() {
    }

    /** Wie viel Zeit das Ticket hat, Feiertage und Wochenenden schon eingerechnet. */
    public static Duration frist(Ticket ticket, Kalender kalender) {
        Duration zusage = ticket.prioritaet.frist(ticket.kategorie.slaStunden());
        LocalDate erster = ticket.gemeldetAm.toLocalDate();
        LocalDate letzter = ticket.gemeldetAm.plus(zusage).toLocalDate().plusDays(1);
        long frei = erster.datesUntil(letzter).filter(kalender::arbeitsfrei).count();
        return zusage.plusDays(frei);
    }

    /** Wann die Zusage endet. */
    public static LocalDateTime faelligAm(Ticket ticket, Kalender kalender) {
        return ticket.gemeldetAm.plus(frist(ticket, kalender));
    }

    /** Erledigte Tickets: rechtzeitig? Offene: noch im Rahmen? */
    public static boolean inDerZusage(Ticket ticket, Kalender kalender, LocalDateTime jetzt) {
        LocalDateTime ende = ticket.erledigtAm != null ? ticket.erledigtAm : jetzt;
        return !ende.isAfter(faelligAm(ticket, kalender));
    }
}
