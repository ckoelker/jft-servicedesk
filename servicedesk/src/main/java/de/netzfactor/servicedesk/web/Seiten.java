package de.netzfactor.servicedesk.web;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import java.util.List;

/**
 * Die Vorlagen unter templates/Seiten - als Methoden mit Typen.
 *
 * <p>Ueber @CheckedTemplate prueft der Bau schon, ob die Vorlage nur Felder
 * anspricht, die es wirklich gibt.
 */
@CheckedTemplate(basePath = "Seiten")
public class Seiten {

    public static native TemplateInstance tickets(List<Zeile> zeilen, Beschriftung beschriftung);

    public static native TemplateInstance teile(List<Zeile> zeilen);

    public static native TemplateInstance anmeldung(String titel, String benutzername,
                                                    String passwort, String senden,
                                                    String hinweis, String fehler);
}
