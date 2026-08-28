package de.netzfactor.servicedesk.ki;

import de.netzfactor.servicedesk.Ticketverwaltung;
import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.dto.NeuerKommentar;
import de.netzfactor.servicedesk.dto.NeuesTicket;
import de.netzfactor.servicedesk.dto.TicketAnsicht;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/** Eine gewoehnliche CDI-Bean - erst die Annotation @Tool macht aus einer Methode ein Werkzeug, das das Modell aufrufen darf. */
@ApplicationScoped
public class Ticketwerkzeuge {

    private final Ticketverwaltung verwaltung;

    public Ticketwerkzeuge(Ticketverwaltung verwaltung) {
        this.verwaltung = verwaltung;
    }

    @Tool("Liefert Kennung, Titel, Kategorie, Prioritaet, Status, Firma, Melder und Bearbeiter"
          + " eines Tickets. Kennungen haben die Form S-0007.")
    @Transactional
    public String ticketNachschlagen(String kennung) {
        Ticket ticket = Ticket.nach(kennung);
        if (ticket == null) {
            return "Ein Ticket mit der Kennung " + kennung + " gibt es nicht.";
        }
        // Alles steht am Ticket selbst - kein Nachladen, kein Join.
        return "%s: %s (Kategorie %s, Prioritaet %s, Status %s, Firma %s, gemeldet von %s, Bearbeiter %s)"
                .formatted(ticket.kennung, ticket.titel, ticket.kategorie, ticket.prioritaet,
                           ticket.status, ticket.firma, ticket.melder,
                           ticket.bearbeiter == null ? "niemand" : ticket.bearbeiter);
    }

    @Tool("Legt ein neues Ticket an und liefert dessen Kennung zurueck. Kategorie ist eine von"
          + " ZUGANG, NETZ, DRUCKER, HARDWARE, SOFTWARE, SONSTIGES, Prioritaet eine von"
          + " NIEDRIG, NORMAL, HOCH, KRITISCH.")
    @Transactional
    public String ticketAnlegen(String titel, String firma, String melder,
                                Kategorie kategorie, Prioritaet prioritaet) {
        if (titel == null || titel.isBlank()) {
            return "Ohne Titel laesst sich kein Ticket anlegen - frag den Melder, worum es geht.";
        }
        TicketAnsicht neues = verwaltung.lege(
                new NeuesTicket(titel.strip(), null, prioritaet, kategorie,
                                firma == null || firma.isBlank() ? "Unbekannte Firma" : firma.strip(),
                                melder == null || melder.isBlank() ? "Unbekannt" : melder.strip()));
        return "Ticket " + neues.kennung() + " angelegt: " + neues.titel() + ".";
    }

    @Tool("Setzt die Prioritaet eines Tickets auf NIEDRIG, NORMAL, HOCH oder KRITISCH.")
    @Transactional
    public String prioritaetSetzen(String kennung, Prioritaet neue) {
        if (Ticket.nach(kennung) == null) {
            return "Ein Ticket mit der Kennung " + kennung + " gibt es nicht.";
        }
        verwaltung.setzePrioritaet(kennung, neue);
        return "Ticket " + kennung + " steht jetzt auf " + neue + ".";
    }

    @Tool("Schreibt einen Kommentar an ein Ticket.")
    @Transactional
    public String kommentieren(String kennung, String text) {
        if (Ticket.nach(kennung) == null) {
            return "Ein Ticket mit der Kennung " + kennung + " gibt es nicht.";
        }
        verwaltung.kommentiere(kennung, new NeuerKommentar(text, "Assistent"));
        return "Kommentar an " + kennung + " geschrieben.";
    }
}
