package de.netzfactor.servicedesk.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

import java.util.List;

/** Gebuchte Arbeitszeit - die Grundlage des Auslastungsberichts. */
@Entity
public class Zeitbuchung extends Basis {

    public int minuten;
    public String bearbeiter;

    @ManyToOne(fetch = FetchType.LAZY)
    public Ticket ticket;

    public static List<Zeitbuchung> alle() {
        return listAll();
    }

    public static void loescheZu(Ticket ticket) {
        delete("ticket", ticket);
    }
}
