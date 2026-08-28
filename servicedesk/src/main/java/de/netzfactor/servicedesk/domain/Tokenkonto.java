package de.netzfactor.servicedesk.domain;

import jakarta.persistence.Entity;

import java.time.LocalDate;

/**
 * Was ein Benutzer an einem Tag beim Modell verbraucht hat.
 *
 * <p>Eine Zeile je Benutzer und Tag, angelegt beim ersten Aufruf. Der Tag ist
 * Teil des Schluessels und nicht etwa ein Zaehler, der nachts zurueckgesetzt
 * wird - so braucht es keinen Auftrag, der um Mitternacht laeuft, und man sieht
 * hinterher noch, was gestern war.
 */
@Entity
public class Tokenkonto extends Basis {

    public String benutzername;

    public LocalDate tag;

    public int verbraucht;

    /** Das Konto des Tages, notfalls neu - der Aufrufer steht in einer Transaktion. */
    public static Tokenkonto heute(String benutzername) {
        LocalDate heute = LocalDate.now();
        Tokenkonto konto = find("benutzername = ?1 and tag = ?2", benutzername, heute).firstResult();
        if (konto == null) {
            konto = new Tokenkonto();
            konto.benutzername = benutzername;
            konto.tag = heute;
            konto.verbraucht = 0;
            konto.persist();
        }
        return konto;
    }

    /** Nur lesen, ohne etwas anzulegen: die Frage "wie viel ist schon weg?". */
    public static int verbrauchtHeute(String benutzername) {
        Tokenkonto konto = find("benutzername = ?1 and tag = ?2", benutzername, LocalDate.now())
                .firstResult();
        return konto == null ? 0 : konto.verbraucht;
    }
}
