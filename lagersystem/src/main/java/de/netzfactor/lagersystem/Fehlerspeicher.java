package de.netzfactor.lagersystem;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Der eingebaute Speicherfehler: ein Zwischenspeicher ohne Ausgang.
 *
 * <p>Er sieht aus wie jeder Cache, den man in einem echten Projekt findet -
 * und hat den Fehler, den echte Caches haben: Es kommt etwas hinein, aber
 * nichts geht wieder heraus. Weil die Bohne {@code @ApplicationScoped} ist,
 * lebt sie so lange wie die Anwendung, und alles darin lebt mit. Genau das
 * macht sie im Heap-Dump zur Wurzel, an der die Megabytes haengen.
 *
 * <p>Das ist kein konstruiertes Beispiel: Ein Cache ohne Verfallsdatum, ein
 * Listener, der nie abgemeldet wird, und eine statische Liste, die mitschreibt,
 * sind die drei haeufigsten Speicherlecks in Java ueberhaupt.
 */
@ApplicationScoped
public class Fehlerspeicher {

    private static final Logger LOG = LogManager.getLogger(Fehlerspeicher.class);

    /** Ein Block ist ein Megabyte - eine Groesse, die man im Diagramm sieht. */
    public static final int BLOCKGROESSE = 1024 * 1024;

    private final Map<String, byte[]> abgelegt = new LinkedHashMap<>();

    private long lfd;

    /** Legt {@code bloecke} Megabyte ab und gibt sie nie wieder her. */
    public synchronized int lege(int bloecke) {
        for (int i = 0; i < bloecke; i++) {
            abgelegt.put("bestandsabfrage-" + (++lfd), new byte[BLOCKGROESSE]);
        }
        LOG.warn("Zwischenspeicher gewachsen auf {} Eintraege (~{} MB)",
                abgelegt.size(), abgelegt.size());
        return abgelegt.size();
    }

    public synchronized int eintraege() {
        return abgelegt.size();
    }

    /** Der Ausgang, den es im Original nicht gibt - fuer die Gegenprobe. */
    public synchronized int leere() {
        int vorher = abgelegt.size();
        abgelegt.clear();
        LOG.info("Zwischenspeicher geleert: {} Eintraege freigegeben", vorher);
        return vorher;
    }
}
