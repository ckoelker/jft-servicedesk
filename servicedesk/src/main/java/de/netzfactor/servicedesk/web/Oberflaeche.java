package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.Ticketverwaltung;
import de.netzfactor.servicedesk.domain.Benutzer;
import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.dto.NeuesTicket;
import de.netzfactor.servicedesk.dto.TicketAnsicht;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.Locale;

/**
 * Die Oberflaeche: einmal die ganze Seite, sonst nur die Tabellenzeilen.
 *
 * <p>Dieselbe Ressource liefert beides, weil htmx nach jeder Aktion genau das
 * Stueck zurueckbekommen soll, das es austauscht - nicht die Seite noch einmal.
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
@RolesAllowed("bearbeiter")
public class Oberflaeche {

    private final Texte texte;
    private final Ticketverwaltung verwaltung;
    private final SecurityIdentity identitaet;

    public Oberflaeche(Texte texte, Ticketverwaltung verwaltung, SecurityIdentity identitaet) {
        this.texte = texte;
        this.verwaltung = verwaltung;
        this.identitaet = identitaet;
    }

    /** Englisch, sobald der Browser es irgendwo nennt - sonst bleibt es deutsch. */
    static Locale sprache(HttpHeaders kopf) {
        for (Locale gewuenschte : kopf.getAcceptableLanguages()) {
            if (Locale.ENGLISH.getLanguage().equals(gewuenschte.getLanguage())) {
                return Locale.ENGLISH;
            }
        }
        return Locale.GERMAN;
    }

    @GET
    @Transactional
    public TemplateInstance seite(@Context HttpHeaders kopf,
                                  @QueryParam("filter") @DefaultValue("alle") String filter) {
        Locale sprache = sprache(kopf);
        long gesamt = Ticket.count();
        long offen = Ticket.count("status <> ?1", Status.ERLEDIGT);

        return Seiten.tickets(zeilen(filter, sprache),
                              Beschriftung.fuer(texte, sprache, gesamt, offen,
                                                anzeigename(),
                                                identitaet.hasRole("assistent")));
    }

    /** Der volle Name steht an der Entity, nicht in der Identitaet - die kennt nur die Anmeldung. */
    private String anzeigename() {
        Benutzer angemeldet = Benutzer.nach(identitaet.getPrincipal().getName());
        return angemeldet == null ? identitaet.getPrincipal().getName() : angemeldet.anzeigename;
    }

    @GET
    @Path("/teile")
    public TemplateInstance teile(@Context HttpHeaders kopf,
                                  @QueryParam("filter") @DefaultValue("alle") String filter) {
        return Seiten.teile(zeilen(filter, sprache(kopf)));
    }

    /**
     * Das Formular schickt seine Felder als application/x-www-form-urlencoded -
     * ohne eine Zeile JavaScript, htmx tauscht danach nur die Tabelle aus.
     */
    @POST
    @Path("/aktion/neu")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance lege(@Context HttpHeaders kopf,
                                 @QueryParam("filter") @DefaultValue("alle") String filter,
                                 @FormParam("betreff") String betreff,
                                 @FormParam("firma") String firma,
                                 @FormParam("melder") String melder,
                                 @FormParam("kategorie") @DefaultValue("SONSTIGES") Kategorie kategorie,
                                 @FormParam("prioritaet") @DefaultValue("NORMAL") Prioritaet prioritaet) {

        // Ein leeres Formular ist kein Fehler, sondern einfach nichts zu tun.
        if (betreff != null && !betreff.isBlank()) {
            verwaltung.lege(new NeuesTicket(betreff.strip(), null, prioritaet, kategorie,
                                            leerIst(firma, "Unbekannte Firma"),
                                            leerIst(melder, "Unbekannt")));
        }
        return Seiten.teile(zeilen(filter, sprache(kopf)));
    }

    private static String leerIst(String wert, String ersatz) {
        return wert == null || wert.isBlank() ? ersatz : wert.strip();
    }

    @POST
    @Path("/aktion/{kennung}/erledigt")
    public TemplateInstance erledige(@Context HttpHeaders kopf,
                                     @PathParam("kennung") String kennung,
                                     @QueryParam("filter") @DefaultValue("alle") String filter) {
        verwaltung.setzeStatus(kennung, Status.ERLEDIGT);
        return Seiten.teile(zeilen(filter, sprache(kopf)));
    }

    @POST
    @Path("/aktion/{kennung}/hoeher")
    public TemplateInstance stufeHoeher(@Context HttpHeaders kopf,
                                        @PathParam("kennung") String kennung,
                                        @QueryParam("filter") @DefaultValue("alle") String filter) {
        verwaltung.setzePrioritaet(kennung, hoeher(hole(kennung).prioritaet));
        return Seiten.teile(zeilen(filter, sprache(kopf)));
    }

    @DELETE
    @Path("/aktion/{kennung}")
    public TemplateInstance loesche(@Context HttpHeaders kopf,
                                    @PathParam("kennung") String kennung,
                                    @QueryParam("filter") @DefaultValue("alle") String filter) {
        verwaltung.loesche(kennung);
        return Seiten.teile(zeilen(filter, sprache(kopf)));
    }

    private List<Zeile> zeilen(String filter, Locale sprache) {
        List<Ticket> treffer = switch (filter) {
            case "offen" -> Ticket.offene();
            case "kritisch" -> Ticket.kritische();
            default -> Ticket.alle();
        };

        return treffer.stream()
                      .map(TicketAnsicht::von)
                      .map(ansicht -> Zeile.von(ansicht, texte, sprache))
                      .toList();
    }

    /** Eine Stufe hoch, und oben ist Schluss - KRITISCH bleibt KRITISCH. */
    private static Prioritaet hoeher(Prioritaet jetzige) {
        Prioritaet[] stufen = Prioritaet.values();
        return stufen[Math.min(jetzige.ordinal() + 1, stufen.length - 1)];
    }

    private static Ticket hole(String kennung) {
        Ticket ticket = Ticket.nach(kennung);
        if (ticket == null) {
            throw new NotFoundException("Kein Ticket " + kennung);
        }
        return ticket;
    }
}
