package de.netzfactor.servicedesk.ki;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Beantwortet dem {@link Inhaltswaechter} die eine Frage, ob eine Eingabe
 * unzulaessig ist.
 *
 * <p>Gefragt wird das Chatmodell, das ohnehin schon angebunden ist - kein
 * zweiter Dienst, kein zweiter Zugang, keine zweite Freischaltung. Was als
 * unzulaessig gilt, steht unten als Text und laesst sich im Kurs vor aller
 * Augen aendern. Der Preis: es kommt ein Wort zurueck und keine Kategorien mit
 * Punktzahlen, wie ein spezialisierter Moderationsdienst sie liefern wuerde.
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
