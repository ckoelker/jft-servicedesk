package de.netzfactor.servicedesk.ki;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;

/**
 * Die Moderations-API von OpenAI - ein Aufruf, ein boolescher Wert.
 *
 * <p>Ein Interface statt eines HttpClient: die Erweiterung baut daraus zur
 * Bauzeit die Umsetzung. Der Schluessel geht als Kopfzeile mit, weil derselbe
 * Schluessel schon fuer das Chatmodell in der Umgebung steht.
 */
@RegisterRestClient(configKey = "moderation")
@Path("/v1/moderations")
public interface Moderationsklient {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    Antwort pruefe(@HeaderParam("Authorization") String autorisierung, Anfrage anfrage);

    record Anfrage(String model, String input) {
    }

    /** Nur das eine Feld, auf das es ankommt - der Rest der Antwort interessiert hier nicht. */
    record Antwort(List<Ergebnis> results) {

        record Ergebnis(boolean flagged) {
        }

        boolean beanstandet() {
            return results != null && !results.isEmpty() && results.getFirst().flagged();
        }
    }
}
