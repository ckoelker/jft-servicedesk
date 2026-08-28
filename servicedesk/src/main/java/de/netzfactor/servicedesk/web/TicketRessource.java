package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.Ticketstrom;
import de.netzfactor.servicedesk.Ticketverwaltung;
import de.netzfactor.servicedesk.auswertung.Berichte;
import de.netzfactor.servicedesk.auswertung.Berichtsschreiber;
import de.netzfactor.servicedesk.auswertung.Feiertage;
import de.netzfactor.servicedesk.domain.Kommentar;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.domain.Zeitbuchung;
import de.netzfactor.servicedesk.dto.Ereignis;
import de.netzfactor.servicedesk.dto.Importergebnis;
import de.netzfactor.servicedesk.dto.KommentarAnsicht;
import de.netzfactor.servicedesk.dto.NeuerKommentar;
import de.netzfactor.servicedesk.dto.NeuesTicket;
import de.netzfactor.servicedesk.dto.TicketAnsicht;
import de.netzfactor.servicedesk.lager.Ersatzteilpruefer;
import io.smallrye.common.annotation.RunOnVirtualThread;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestStreamElementType;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

/**
 * Die Schnittstelle nach draussen. Sie kennt nur Ansichten, keine Entities.
 *
 * <p>Die Rolle steht hier an der Klasse und nicht in der
 * application.properties: dort ist geregelt, dass ueberhaupt jemand angemeldet
 * sein muss, hier, welche Rolle dieser Jemand braucht. So sieht man die
 * Anforderung dort, wo der Endpunkt steht.
 */
@Path("/tickets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("bearbeiter")
public class TicketRessource {

    private static final String EXCEL =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final Ticketverwaltung verwaltung;
    private final Ticketstrom strom;
    private final Ersatzteilpruefer pruefer;
    private final Feiertage feiertage;

    public TicketRessource(Ticketverwaltung verwaltung, Ticketstrom strom,
                           Ersatzteilpruefer pruefer, Feiertage feiertage) {
        this.verwaltung = verwaltung;
        this.strom = strom;
        this.pruefer = pruefer;
        this.feiertage = feiertage;
    }

    /** Ohne Parameter alles, sonst gefiltert - die fehlenden Parameter kommen als null an. */
    @GET
    public List<TicketAnsicht> alle(@QueryParam("firma") String firma,
                                    @QueryParam("status") Status status,
                                    @QueryParam("prioritaet") Prioritaet prioritaet) {
        List<Ticket> treffer = firma == null ? Ticket.alle() : Ticket.vonFirma(firma);

        return treffer.stream()
                      .filter(ticket -> status == null || ticket.status == status)
                      .filter(ticket -> prioritaet == null || ticket.prioritaet == prioritaet)
                      .map(TicketAnsicht::von)
                      .toList();
    }

    @GET
    @Path("/kritisch")
    public List<TicketAnsicht> kritisch() {
        return Ticket.kritische().stream().map(TicketAnsicht::von).toList();
    }

    /** Server-sent events: der Browser haelt die Verbindung offen und hoert zu. */
    @GET
    @Path("/strom")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Ereignis> ereignisse() {
        return strom.anschluss();
    }

    @GET
    @Path("/{kennung}")
    public TicketAnsicht eines(@PathParam("kennung") String kennung) {
        return TicketAnsicht.von(hole(kennung));
    }

    @POST
    public RestResponse<TicketAnsicht> melde(@Valid NeuesTicket meldung) {
        return RestResponse.status(RestResponse.Status.CREATED, verwaltung.lege(meldung));
    }

    @PUT
    @Path("/{kennung}")
    public TicketAnsicht aendere(@PathParam("kennung") String kennung, @Valid NeuesTicket meldung) {
        return verwaltung.aendere(kennung, meldung);
    }

    @PUT
    @Path("/{kennung}/status/{neuer}")
    public TicketAnsicht setzeStatus(@PathParam("kennung") String kennung,
                                     @PathParam("neuer") Status neuer) {
        return verwaltung.setzeStatus(kennung, neuer);
    }

    @PUT
    @Path("/{kennung}/prioritaet/{neue}")
    public TicketAnsicht setzePrioritaet(@PathParam("kennung") String kennung,
                                         @PathParam("neue") Prioritaet neue) {
        return verwaltung.setzePrioritaet(kennung, neue);
    }

    @DELETE
    @Path("/{kennung}")
    public RestResponse<Void> loesche(@PathParam("kennung") String kennung) {
        verwaltung.loesche(kennung);
        return RestResponse.status(RestResponse.Status.NO_CONTENT);
    }

    @POST
    @Path("/{kennung}/kommentare")
    public RestResponse<KommentarAnsicht> kommentiere(@PathParam("kennung") String kennung,
                                                      @Valid NeuerKommentar neuer) {
        return RestResponse.status(RestResponse.Status.CREATED,
                                   verwaltung.kommentiere(kennung, neuer));
    }

    @GET
    @Path("/{kennung}/kommentare")
    public List<KommentarAnsicht> kommentare(@PathParam("kennung") String kennung) {
        return Kommentar.zu(hole(kennung)).stream().map(KommentarAnsicht::von).toList();
    }

    /** Der Import aus Block 6 am Netz: die CSV steht im Rumpf, das Misslungene in der Antwort. */
    @POST
    @Path("/import")
    @Consumes({MediaType.TEXT_PLAIN, "text/csv"})
    public Importergebnis importiere(String csv) {
        return Importergebnis.von(verwaltung.importiere(new BufferedReader(new StringReader(csv))));
    }

    /**
     * Der Aufruf blockiert, bis das Lagersystem antwortet - auf einem virtual
     * thread kostet das Warten keinen Platform-Thread, und der Server nimmt
     * waehrenddessen weiter Anfragen an.
     */
    @GET
    @Path("/{kennung}/ersatzteil/{nummer}")
    @Produces(MediaType.TEXT_PLAIN)
    @RunOnVirtualThread
    public String ersatzteil(@PathParam("kennung") String kennung,
                             @PathParam("nummer") String nummer) {
        hole(kennung);
        return pruefer.pruefe(nummer);
    }

    @GET
    @Path("/bericht.txt")
    @Produces(MediaType.TEXT_PLAIN)
    public RestResponse<String> berichtAlsText(@QueryParam("art") @DefaultValue("sla") String art) {
        return RestResponse.ok(Berichtsschreiber.alsText(titel(art), daten(art)));
    }

    @GET
    @Path("/bericht.xlsx")
    @Produces(EXCEL)
    public RestResponse<byte[]> berichtAlsExcel(@QueryParam("art") @DefaultValue("sla") String art) {
        return anhang(Berichtsschreiber.alsExcel(titel(art), daten(art)),
                      EXCEL, dateiname(art) + ".xlsx");
    }

    @GET
    @Path("/bericht.pdf")
    @Produces("application/pdf")
    public RestResponse<byte[]> berichtAlsPdf(@QueryParam("art") @DefaultValue("sla") String art) {
        return anhang(Berichtsschreiber.alsPdf(titel(art), daten(art)),
                      "application/pdf", dateiname(art) + ".pdf");
    }

    /** Ohne diesen Kopf zeigt der Browser die Bytes an, statt eine Datei anzubieten. */
    private static RestResponse<byte[]> anhang(byte[] inhalt, String medientyp, String dateiname) {
        return RestResponse.ResponseBuilder.ok(inhalt, medientyp)
                                           .header("Content-Disposition",
                                                   "attachment; filename=\"" + dateiname + "\"")
                                           .build();
    }

    /** Dieselbe Auswertung wie in Block 8 - nur kommen die Daten jetzt aus der Datenbank und gehen ueber HTTP hinaus. */
    private List<?> daten(String art) {
        return switch (art) {
            case "melder" -> Berichte.topMelder(Ticket.alle());
            case "auslastung" -> Berichte.auslastung(Ticket.alle(), Zeitbuchung.alle());
            // Feiertage statt nur Wochenende: der 1. Mai zaehlt sonst als Arbeitstag.
            default -> Berichte.slaQuote(Ticket.alle(), feiertage);
        };
    }

    private static String titel(String art) {
        return switch (art) {
            case "melder" -> "Die haeufigsten Melder";
            case "auslastung" -> "Auslastung der Bearbeiter";
            default -> "SLA-Quote je Kategorie";
        };
    }

    private static String dateiname(String art) {
        return switch (art) {
            case "melder" -> "top-melder";
            case "auslastung" -> "auslastung";
            default -> "sla-quote";
        };
    }

    private static Ticket hole(String kennung) {
        Ticket ticket = Ticket.nach(kennung);
        if (ticket == null) {
            throw new NotFoundException("Kein Ticket " + kennung);
        }
        return ticket;
    }
}
