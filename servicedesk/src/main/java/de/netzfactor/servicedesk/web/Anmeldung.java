package de.netzfactor.servicedesk.web;

import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.Locale;

/**
 * Die Anmeldeseite und der Weg wieder hinaus.
 *
 * <p>Das Formular selbst wird von keiner Methode hier verarbeitet: es geht an
 * {@code /j_security_check}, und das beantwortet Quarkus. Diese Klasse liefert
 * nur die Seite - und traegt beim Abmelden das Sitzungsplaetzchen aus.
 */
@Path("/")
@PermitAll
public class Anmeldung {

    private static final String KEKS = "quarkus-credential";

    private final Texte texte;
    private final SecurityIdentity identitaet;

    public Anmeldung(Texte texte, SecurityIdentity identitaet) {
        this.texte = texte;
        this.identitaet = identitaet;
    }

    @GET
    @Path("/anmelden")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance seite(@Context HttpHeaders kopf,
                                  @QueryParam("fehler") String fehler) {
        Locale sprache = Oberflaeche.sprache(kopf);
        return Seiten.anmeldung(
                texte.text("anmeldung.titel", sprache),
                texte.text("anmeldung.benutzername", sprache),
                texte.text("anmeldung.passwort", sprache),
                texte.text("anmeldung.senden", sprache),
                texte.text("anmeldung.hinweis", sprache),
                fehler == null ? null : texte.text("anmeldung.fehler", sprache));
    }

    /**
     * Abmelden heisst hier: das Plaetzchen loeschen und zurueck zur Anmeldung.
     * Eine Sitzung auf dem Server, die man beenden koennte, gibt es nicht - das
     * Formular-Verfahren von Quarkus legt alles verschluesselt in den Keks.
     */
    @GET
    @Path("/abmelden")
    public Response abmelden() {
        NewCookie geloescht = new NewCookie.Builder(KEKS)
                .path("/")
                .maxAge(0)
                .value("")
                .build();
        return Response.seeOther(URI.create("/anmelden")).cookie(geloescht).build();
    }

    /** Wer angemeldet ist, steht oben auf der Seite - sonst niemand. */
    public String angemeldeterName() {
        return identitaet.isAnonymous() ? null : identitaet.getPrincipal().getName();
    }
}
