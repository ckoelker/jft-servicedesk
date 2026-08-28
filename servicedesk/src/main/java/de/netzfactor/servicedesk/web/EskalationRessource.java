package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.Ticketstrom;
import de.netzfactor.servicedesk.auswertung.Feiertage;
import de.netzfactor.servicedesk.auswertung.Kalender;
import de.netzfactor.servicedesk.auswertung.Sla;
import de.netzfactor.servicedesk.domain.Ticket;
import de.netzfactor.servicedesk.dto.Ereignis;
import de.netzfactor.servicedesk.dto.Eskalationslauf;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Der Eskalationslauf aus Block 14 am Netz: welche offenen Tickets haben die
 * Zusage gerissen?
 *
 * <p>Es wird nur gelesen und gemeldet - deshalb steht hier kein
 * {@code @Transactional}.
 */
@Path("/eskalation")
@Produces(MediaType.APPLICATION_JSON)
public class EskalationRessource {

    private final Ticketstrom strom;
    private final Feiertage feiertage;

    public EskalationRessource(Ticketstrom strom, Feiertage feiertage) {
        this.strom = strom;
        this.feiertage = feiertage;
    }

    @POST
    public Eskalationslauf laufe() {
        List<Ticket> offene = Ticket.offene();
        // Der echte Feiertagsdienst: faellt er aus, zaehlt er nur das Wochenende
        // und der Lauf geht trotzdem durch.
        Kalender kalender = feiertage;
        LocalDateTime jetzt = LocalDateTime.now();

        // AtomicLong statt zaehler++: acht Threads erhoehen denselben Zaehler, und
        // ++ ist drei Schritte - zwei Threads lesen denselben alten Wert, eine
        // Erhoehung geht verloren.
        AtomicLong ueberfaellig = new AtomicLong();

        List<Callable<String>> aufgaben = offene.stream()
                .map(ticket -> (Callable<String>) () -> {
                    if (Sla.inDerZusage(ticket, kalender, jetzt)) {
                        return null;
                    }
                    ueberfaellig.incrementAndGet();
                    strom.melde(Ereignis.geaendert(ticket));
                    return ticket.kennung;
                })
                .toList();

        List<String> kennungen;
        ExecutorService dienst = Executors.newFixedThreadPool(8);
        try {
            kennungen = dienst.invokeAll(aufgaben).stream()
                              .map(EskalationRessource::wert)
                              .filter(kennung -> kennung != null)
                              .toList();
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Der Eskalationslauf wurde unterbrochen", fehler);
        } finally {
            dienst.shutdown();
        }

        return new Eskalationslauf(offene.size(), (int) ueberfaellig.get(), kennungen);
    }

    /** invokeAll wartet auf alle Aufgaben - hier kann get() also nur noch fertige Werte liefern. */
    private static String wert(Future<String> fertig) {
        try {
            return fertig.get();
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Der Eskalationslauf wurde unterbrochen", fehler);
        } catch (ExecutionException fehler) {
            throw new IllegalStateException("Die Pruefung ist gescheitert", fehler.getCause());
        }
    }
}
