package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.dto.TicketAnsicht;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

/**
 * Eine Tabellenzeile, fertig uebersetzt und formatiert.
 *
 * <p>Die Vorlage soll nichts mehr entscheiden muessen: der Schluessel bleibt
 * nur fuer die Farbe des Punktes erhalten.
 */
public record Zeile(String kennung, String titel,
                    String prioritaet, String prioritaetSchluessel,
                    String status, String statusSchluessel,
                    String kategorie,
                    String firma, String gemeldet, String bearbeiter) {

    public static Zeile von(TicketAnsicht ansicht, Texte texte, Locale sprache) {
        DateTimeFormatter datum = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                                                   .withLocale(sprache);

        return new Zeile(ansicht.kennung(),
                         ansicht.titel(),
                         texte.text("prioritaet." + ansicht.prioritaet().name(), sprache),
                         ansicht.prioritaet().name(),
                         texte.text("status." + ansicht.status().name(), sprache),
                         ansicht.status().name(),
                         texte.text("kategorie." + ansicht.kategorie().name(), sprache),
                         ansicht.firma(),
                         ansicht.gemeldetAm() == null ? "" : datum.format(ansicht.gemeldetAm()),
                         ansicht.bearbeiter() == null
                                 ? texte.text("niemand", sprache)
                                 : ansicht.bearbeiter());
    }
}
