package de.netzfactor.servicedesk;

import de.netzfactor.servicedesk.dto.Ereignis;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;

/** Die Stelle, an der jede Aenderung einmal gemeldet und beliebig oft gehoert wird. */
@ApplicationScoped
public class Ticketstrom {

    // BroadcastProcessor, weil ein Multi.createFrom() seine Ereignisse beim Anlegen
    // schon kennen muesste - hier kommen sie erst spaeter und fuer mehrere Zuhoerer.
    private final BroadcastProcessor<Ereignis> strom = BroadcastProcessor.create();

    public void melde(Ereignis ereignis) {
        strom.onNext(ereignis);
    }

    public Multi<Ereignis> anschluss() {
        return strom;
    }
}
