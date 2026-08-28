package de.netzfactor.servicedesk.web;

import jakarta.enterprise.context.ApplicationScoped;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Jeder sichtbare Text der Oberflaeche - nachgeschlagen, nicht einprogrammiert.
 *
 * <p>Aufgeloest wird vor dem Rendern, damit die Vorlage nur noch fertige
 * Zeichenketten einsetzt und ein Fragment ohne eigene Sprachlogik auskommt.
 */
@ApplicationScoped
public class Texte {

    private static final String BASIS = "meldungen";

    public String text(String schluessel, Locale sprache) {
        return buendel(sprache).getString(schluessel);
    }

    /** Dieselbe Nachschlage, aber mit Platzhaltern {0}, {1} - Zahlen kommen in der Sprache heraus. */
    public String text(String schluessel, Locale sprache, Object... werte) {
        return new MessageFormat(text(schluessel, sprache), sprache).format(werte);
    }

    private ResourceBundle buendel(Locale sprache) {
        // Ohne Rueckfall: sonst liefert ein deutsch eingestellter Rechner auf eine
        // englische Anfrage die deutschen Texte, weil das Bundle darauf ausweicht.
        return ResourceBundle.getBundle(
                BASIS,
                sprache,
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
    }
}
