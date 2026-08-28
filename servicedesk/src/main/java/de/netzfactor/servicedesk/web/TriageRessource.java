package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.Ticketverwaltung;
import de.netzfactor.servicedesk.domain.Kommentar;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.dto.NeuesTicket;
import de.netzfactor.servicedesk.dto.TicketAnsicht;
import de.netzfactor.servicedesk.dto.Triage;
import de.netzfactor.servicedesk.ki.Triagedienst;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;

/** Die Triage aus Block 16 am Netz: Meldung im Klartext hinein, eingeordnetes Ticket heraus. */
@Path("/triage")
public class TriageRessource {

    private final Triagedienst dienst;
    private final Ticketverwaltung verwaltung;

    public TriageRessource(Triagedienst dienst, Ticketverwaltung verwaltung) {
        this.dienst = dienst;
        this.verwaltung = verwaltung;
    }

    /** Die Antwort ist ein record, kein Text - das Modell fuellt das Schema aus, das LangChain4j daraus baut. */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Triage einordnen(String meldung) {
        return dienst.einordnen(meldung);
    }

    @POST
    @Path("/ticket")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public RestResponse<TicketAnsicht> alsTicket(
            @QueryParam("firma") @DefaultValue("Nordlicht Werften GmbH") String firma,
            @QueryParam("melder") @DefaultValue("Anke Brehm") String melder,
            String meldung) {

        Triage triage = dienst.einordnen(meldung);
        NeuesTicket neues = new NeuesTicket(triage.titel(), meldung, triage.prioritaet(),
                                            triage.kategorie(), firma, melder);

        return RestResponse.status(RestResponse.Status.CREATED, verwaltung.lege(neues));
    }

    /**
     * Transaktional, damit Ticket und Kommentare in einer offenen Sitzung gelesen
     * werden - ohne sie reisst der Zugriff auf den Verlauf ab.
     */
    @GET
    @Path("/{kennung}/zusammenfassung")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String zusammenfassung(@PathParam("kennung") String kennung) {
        Ticket ticket = Ticket.nach(kennung);
        if (ticket == null) {
            throw new NotFoundException("Kein Ticket " + kennung);
        }

        StringBuilder verlauf = new StringBuilder(ticket.titel);
        verlauf.append('\n').append(ticket.beschreibung == null ? "" : ticket.beschreibung);
        for (Kommentar kommentar : Kommentar.zu(ticket)) {
            verlauf.append('\n').append(kommentar.autor).append(": ").append(kommentar.text);
        }

        return dienst.zusammenfassen(verlauf.toString());
    }
}
