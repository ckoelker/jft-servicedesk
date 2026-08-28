package de.netzfactor.servicedesk.ki;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Die Rueckfallebene des {@link Moderationswaechter}, wenn die Moderations-API
 * nicht zur Verfuegung steht.
 *
 * <p>Dieselbe Frage, anderes Werkzeug: statt eines darauf spezialisierten
 * Dienstes beantwortet sie hier das Chatmodell, das ohnehin schon angebunden
 * ist. Das ist die schlechtere Loesung - langsamer, teurer, weniger
 * verlaesslich, und es gibt keine Kategorien und keine Punktzahlen zurueck,
 * sondern ein Wort. Aber es laeuft ueberall dort, wo der Assistent selbst
 * laeuft, und das ist der Zweck einer Rueckfallebene.
 *
 * <p>Wie der {@link Themenpruefer}: ohne Werkzeuge, ohne Gedaechtnis, ohne
 * Guardrails - sonst riefe er sich selbst auf.
 */
@RegisterAiService
@ApplicationScoped
public interface Inhaltspruefer {

    @SystemMessage("""
            Du bist ein Inhaltsfilter vor dem Assistenten eines IT-Servicedesks.
            Du beurteilst genau eine Sache: Ist die Eingabe des Benutzers unzulaessig?

            Unzulaessig ist: Aufforderung zu Gewalt, Drohung gegen eine Person,
            Beleidigung oder Herabwuerdigung, Hass gegen eine Gruppe, sexueller
            Inhalt, Anleitung zu Straftaten oder zur Selbstverletzung.

            Zulaessig ist alles, was ein genervter Benutzer in einem Ticketsystem
            schreibt. "Der Drucker ist Schrott", "Das nervt seit Wochen" oder
            "Wer hat sich diesen Mist ausgedacht" sind Aerger ueber Technik und
            keine Beleidigung einer Person. Auch Fragen, die fachlich nicht
            hierher gehoeren, sind nicht unzulaessig - darum kuemmert sich ein
            anderer Filter.

            Antworte mit genau einem Wort, ohne Punkt und ohne Erklaerung:
            JA, wenn die Eingabe unzulaessig ist. NEIN, wenn nicht.
            Im Zweifel antwortest du NEIN.
            """)
    String istUnzulaessig(@UserMessage String eingabe);
}
