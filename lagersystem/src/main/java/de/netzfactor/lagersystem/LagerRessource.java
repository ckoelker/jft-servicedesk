package de.netzfactor.lagersystem;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

/** Die Schnittstelle des Lagersystems: zwei Abfragen, beide mit Wartezeit. */
@Path("/teile")
@Produces(MediaType.APPLICATION_JSON)
public class LagerRessource {

    private static final Logger LOG = LogManager.getLogger(LagerRessource.class);

    private final Lager lager;
    private final long verzoegerung;

    public LagerRessource(Lager lager,
                          @ConfigProperty(name = "lagersystem.verzoegerung") long verzoegerung) {
        this.lager = lager;
        this.verzoegerung = verzoegerung;
    }

    @GET
    public List<Teil> alle() {
        LOG.info("GET /teile");
        warte();
        return lager.alle();
    }

    @GET
    @Path("/{nummer}")
    public Teil nach(@PathParam("nummer") String nummer) {
        LOG.info("GET /teile/{}", nummer);
        warte();
        return lager.nach(nummer)
                .orElseThrow(() -> new NotFoundException("Unbekanntes Teil: " + nummer));
    }

    // Die Wartezeit ist der ganze Zweck dieses Systems: erst ein langsamer
    // Dienst macht sichtbar, was Nebenlaeufigkeit beim Aufrufer bringt.
    private void warte() {
        try {
            Thread.sleep(verzoegerung);
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Warten wurde unterbrochen", fehler);
        }
    }
}
