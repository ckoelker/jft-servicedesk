package de.netzfactor.servicedesk.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Der Vorgang, um den sich alles dreht.
 *
 * <p>Firma, Melder und Bearbeiter stehen als Werte hier, nicht als Verweis auf
 * eine Stammdatentabelle - fuer diese Woche waeren das drei Joins ohne
 * Erkenntnisgewinn.
 */
@Entity
public class Ticket extends Basis {

    public String kennung;
    public String titel;

    @Column(length = 2000)
    public String beschreibung;

    public String firma;
    public String melder;

    @Enumerated(EnumType.STRING)
    public Kategorie kategorie;

    @Enumerated(EnumType.STRING)
    public Prioritaet prioritaet;

    @Enumerated(EnumType.STRING)
    public Status status;

    /** Leer, solange niemand zustaendig ist - der haeufigste Grund fuer eine gerissene Zusage. */
    public String bearbeiter;

    public LocalDateTime gemeldetAm;
    public LocalDateTime erledigtAm;

    public static Ticket nach(String kennung) {
        return find("kennung", kennung).firstResult();
    }

    public static List<Ticket> alle() {
        return find("order by gemeldetAm desc").list();
    }

    public static List<Ticket> offene() {
        return find("status <> ?1 order by gemeldetAm desc", Status.ERLEDIGT).list();
    }

    public static List<Ticket> kritische() {
        return find("prioritaet = ?1 and status <> ?2 order by gemeldetAm desc",
                    Prioritaet.KRITISCH, Status.ERLEDIGT).list();
    }

    public static List<Ticket> vonFirma(String firma) {
        return find("firma = ?1 order by gemeldetAm desc", firma).list();
    }

    /** Die naechste freie Kennung - hoechste vergebene Nummer plus eins. */
    public static String naechsteKennung() {
        Ticket letztes = find("order by id desc").firstResult();
        long naechste = letztes == null ? 1 : Long.parseLong(letztes.kennung.substring(2)) + 1;
        return String.format("S-%04d", naechste);
    }
}
