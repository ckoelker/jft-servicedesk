package de.netzfactor.servicedesk;

import de.netzfactor.servicedesk.dto.Ergebnis;
import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Kommentar;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.domain.Zeitbuchung;
import de.netzfactor.servicedesk.dto.Ereignis;
import de.netzfactor.servicedesk.dto.KommentarAnsicht;
import de.netzfactor.servicedesk.dto.Meldung;
import de.netzfactor.servicedesk.dto.NeuerKommentar;
import de.netzfactor.servicedesk.dto.NeuesTicket;
import de.netzfactor.servicedesk.dto.TicketAnsicht;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Alles, was an einem Ticket geaendert werden kann - an einer Stelle.
 *
 * <p>Jede Aenderung geht ueber den {@link Ticketstrom} hinaus, damit die
 * Oberflaeche nicht fragen muss, ob sich etwas getan hat.
 *
 * <p>Zurueck kommt immer eine Ansicht, nie die Entity: nach dem Ende der
 * Transaktion ist die Sitzung zu, und jeder lazy Verweis wuerde draussen
 * platzen.
 */
@ApplicationScoped
public class Ticketverwaltung {

    private final Ticketstrom strom;

    public Ticketverwaltung(Ticketstrom strom) {
        this.strom = strom;
    }

    @Transactional
    public TicketAnsicht lege(NeuesTicket meldung) {
        Ticket ticket = new Ticket();
        ticket.kennung = Ticket.naechsteKennung();
        ticket.status = Status.NEU;
        ticket.gemeldetAm = LocalDateTime.now();
        // Zustaendig ist zunaechst niemand - die Zuordnung ist ein eigener Schritt.
        ticket.bearbeiter = null;
        uebernimm(ticket, meldung);
        ticket.persist();

        strom.melde(Ereignis.angelegt(ticket));
        return TicketAnsicht.von(ticket);
    }

    @Transactional
    public TicketAnsicht aendere(String kennung, NeuesTicket meldung) {
        Ticket ticket = hole(kennung);
        uebernimm(ticket, meldung);

        strom.melde(Ereignis.geaendert(ticket));
        return TicketAnsicht.von(ticket);
    }

    @Transactional
    public TicketAnsicht setzeStatus(String kennung, Status neuer) {
        Ticket ticket = hole(kennung);
        ticket.status = neuer;
        // Der Zeitpunkt haengt am Status: wer ein Ticket wieder oeffnet, hat es nicht erledigt.
        ticket.erledigtAm = neuer == Status.ERLEDIGT ? LocalDateTime.now() : null;

        strom.melde(Ereignis.geaendert(ticket));
        return TicketAnsicht.von(ticket);
    }

    @Transactional
    public TicketAnsicht setzePrioritaet(String kennung, Prioritaet neue) {
        Ticket ticket = hole(kennung);
        ticket.prioritaet = neue;

        strom.melde(Ereignis.geaendert(ticket));
        return TicketAnsicht.von(ticket);
    }

    @Transactional
    public KommentarAnsicht kommentiere(String kennung, NeuerKommentar neuer) {
        Ticket ticket = hole(kennung);

        Kommentar kommentar = new Kommentar();
        kommentar.ticket = ticket;
        kommentar.autor = neuer.autor();
        kommentar.text = neuer.text();
        kommentar.geschriebenAm = LocalDateTime.now();
        kommentar.persist();

        strom.melde(Ereignis.geaendert(ticket));
        return KommentarAnsicht.von(kommentar);
    }

    @Transactional
    public void loesche(String kennung) {
        Ticket ticket = hole(kennung);
        Ereignis ereignis = Ereignis.geloescht(ticket);

        // Erst die Kinder: der Fremdschluessel auf das Ticket haelt sie sonst fest.
        Kommentar.delete("ticket", ticket);
        Zeitbuchung.loescheZu(ticket);
        ticket.delete();

        strom.melde(ereignis);
    }

    /**
     * Legt fuer jede gelungene Zeile ein Ticket an und gibt trotzdem die ganze
     * Liste zurueck - das Misslungene ist ein Wert im Ergebnis, kein Abbruch,
     * damit neun gute Zeilen nicht an der zehnten kaputten scheitern.
     */
    @Transactional
    public List<Ergebnis<Meldung>> importiere(BufferedReader quelle) {
        List<Ergebnis<Meldung>> ergebnisse = Meldungsimport.lies(quelle);

        for (Ergebnis<Meldung> ergebnis : ergebnisse) {
            if (ergebnis instanceof Ergebnis.Gelungen<Meldung> gelungen) {
                Meldung meldung = gelungen.wert();
                lege(new NeuesTicket(meldung.titel(), "", meldung.prioritaet(),
                                     Kategorie.SONSTIGES, meldung.firma(), meldung.melder()));
            }
        }
        return ergebnisse;
    }

    private void uebernimm(Ticket ticket, NeuesTicket meldung) {
        ticket.titel = meldung.titel();
        ticket.beschreibung = meldung.beschreibung();
        ticket.prioritaet = meldung.prioritaet();
        ticket.kategorie = meldung.kategorie();
        ticket.firma = meldung.firma();
        ticket.melder = meldung.melder();
    }

    private Ticket hole(String kennung) {
        Ticket ticket = Ticket.nach(kennung);
        if (ticket == null) {
            throw new NotFoundException("Kein Ticket " + kennung);
        }
        return ticket;
    }
}
