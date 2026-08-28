package de.netzfactor.servicedesk.web;

import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Alle Beschriftungen der Seite in einem Stueck.
 *
 * <p>So steht in der Vorlage kein einziger Nachschlage-Aufruf und kein einziges
 * festes Wort - nur Felder dieses Records.
 */
public record Beschriftung(String sprache,
                           String titel, String untertitel, String zusammenfassung,
                           String spalteKennung, String spalteTitel, String spaltePrioritaet,
                           String spalteStatus, String spalteKategorie,
                           String spalteFirma, String spalteGemeldet,
                           String spalteBearbeiter, String spalteAktion,
                           String aktionErledigt, String aktionHoeher, String aktionLoeschen,
                           String filterAlle, String filterOffen, String filterKritisch,
                           String chatTitel, String chatPlatzhalter, String chatSenden,
                           String chatDenkt,
                           String stromVerbunden, String stromGetrennt,
                           String berichtTitel,
                           String neuTitel, String neuBetreff, String neuFirma,
                           String neuMelder, String neuAnlegen,
                           String angemeldetAls, String abmelden,
                           boolean darfAssistent,
                           List<Wahl> kategorien, List<Wahl> prioritaeten) {

    /** Ein Eintrag in einem Auswahlfeld: der Schluessel geht ans Formular, der Text an den Leser. */
    public record Wahl(String schluessel, String text) {
    }

    /**
     * @param anzeigename    wer angemeldet ist - steht oben rechts auf der Seite
     * @param darfAssistent  ob die Rolle "assistent" dabei ist. Nur davon haengt
     *                       ab, ob der Kasten mit dem Chat ueberhaupt gerendert
     *                       wird. Die Ressource dahinter ist zusaetzlich mit
     *                       {@code @RolesAllowed} gesichert - was man nicht
     *                       sieht, darf man deshalb trotzdem nicht aufrufen.
     */
    public static Beschriftung fuer(Texte texte, Locale sprache, long gesamt, long offen,
                                    String anzeigename, boolean darfAssistent) {
        return new Beschriftung(sprache.getLanguage(),
                                texte.text("seite.titel", sprache),
                                texte.text("seite.untertitel", sprache),
                                texte.text("zusammenfassung", sprache, gesamt, offen),
                                texte.text("spalte.kennung", sprache),
                                texte.text("spalte.titel", sprache),
                                texte.text("spalte.prioritaet", sprache),
                                texte.text("spalte.status", sprache),
                                texte.text("spalte.kategorie", sprache),
                                texte.text("spalte.firma", sprache),
                                texte.text("spalte.gemeldet", sprache),
                                texte.text("spalte.bearbeiter", sprache),
                                texte.text("spalte.aktion", sprache),
                                texte.text("aktion.erledigt", sprache),
                                texte.text("aktion.hoeher", sprache),
                                texte.text("aktion.loeschen", sprache),
                                texte.text("filter.alle", sprache),
                                texte.text("filter.offen", sprache),
                                texte.text("filter.kritisch", sprache),
                                texte.text("chat.titel", sprache),
                                texte.text("chat.platzhalter", sprache),
                                texte.text("chat.senden", sprache),
                                texte.text("chat.denkt", sprache),
                                texte.text("strom.verbunden", sprache),
                                texte.text("strom.getrennt", sprache),
                                texte.text("bericht.titel", sprache),
                                texte.text("neu.titel", sprache),
                                texte.text("neu.betreff", sprache),
                                texte.text("neu.firma", sprache),
                                texte.text("neu.melder", sprache),
                                texte.text("neu.anlegen", sprache),
                                texte.text("anmeldung.angemeldet", sprache, anzeigename),
                                texte.text("anmeldung.abmelden", sprache),
                                darfAssistent,
                                wahl(Kategorie.values(), "kategorie.", texte, sprache),
                                wahl(Prioritaet.values(), "prioritaet.", texte, sprache));
    }

    /** Die Auswahlfelder werden hier gefuellt, damit die Vorlage keine Enums kennen muss. */
    private static List<Wahl> wahl(Enum<?>[] werte, String vorsilbe, Texte texte, Locale sprache) {
        return Arrays.stream(werte)
                     .map(wert -> new Wahl(wert.name(), texte.text(vorsilbe + wert.name(), sprache)))
                     .toList();
    }
}
