package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.ki.Assistent;
import de.netzfactor.servicedesk.ki.Handbuch;
import dev.langchain4j.guardrail.InputGuardrailException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Der Chat als eine Zeile curl: Frage rein, Antwort raus. */
@Path("/assistent")
@RolesAllowed("assistent")
public class ChatRessource {

    private final Assistent assistent;
    private final Handbuch handbuch;

    public ChatRessource(Assistent assistent, Handbuch handbuch) {
        this.assistent = assistent;
        this.handbuch = handbuch;
    }

    /**
     * Die Sitzung ist der Schluessel des Gedaechtnisses - zwei Namen sind zwei
     * getrennte Gespraeche.
     *
     * <p>Greift ein Waechter ein, wirft LangChain4j eine
     * {@link InputGuardrailException}. Sie ist hier kein Fehler, sondern die
     * Antwort: der Text, den der Waechter mitgegeben hat, geht mit 403 an den
     * Browser und steht dort im Antwortkasten. Ohne dieses Auffangen bekaeme
     * der Benutzer eine 500 und einen Stacktrace im Protokoll.
     */
    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response frage(@QueryParam("sitzung") @DefaultValue("standard") String sitzung, String frage) {
        try {
            return Response.ok(assistent.frage(sitzung, frage)).build();
        } catch (InputGuardrailException abgelehnt) {
            return Response.status(Response.Status.FORBIDDEN)
                           .type(MediaType.TEXT_PLAIN)
                           .entity(grund(abgelehnt))
                           .build();
        }
    }

    /**
     * Die Meldung der Ausnahme traegt den Klassennamen des Waechters vor sich her.
     * Auf der Seite soll aber nur der Satz stehen, den der Waechter geschrieben hat.
     */
    private static String grund(InputGuardrailException abgelehnt) {
        String meldung = abgelehnt.getMessage();
        if (meldung == null || meldung.isBlank()) {
            return "Die Anfrage wurde abgelehnt.";
        }
        int doppelpunkt = meldung.lastIndexOf(": ");
        return doppelpunkt < 0 ? meldung : meldung.substring(doppelpunkt + 2);
    }

    @GET
    @Path("/handbuch")
    @Produces(MediaType.TEXT_PLAIN)
    public String handbuchstand() {
        return handbuch.aufgenommen() + " Abschnitte aufgenommen.";
    }
}
