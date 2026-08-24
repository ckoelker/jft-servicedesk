package de.netzfactor.servicedesk.domain;

import java.time.LocalDateTime;

/**
 * Der Vorgang, um den sich alles dreht.
 *
 * <p>Firma, Melder und Bearbeiter stehen als Werte hier, nicht als Verweis auf
 * eine Stammdatentabelle - fuer diese Woche waeren das drei Joins ohne
 * Erkenntnisgewinn.
 */
public class Ticket {

    public String kennung;
    public String titel;
    public String beschreibung;
    public String firma;
    public String melder;
    public Kategorie kategorie;
    public Prioritaet prioritaet;
    public Status status;

    /** Leer, solange niemand zustaendig ist - der haeufigste Grund fuer eine gerissene Zusage. */
    public String bearbeiter;

    public LocalDateTime gemeldetAm;
    public LocalDateTime erledigtAm;
}
