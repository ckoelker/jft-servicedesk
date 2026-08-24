package de.netzfactor.servicedesk.dto;

import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;

/**
 * Der Rueckgabetyp des KI-Aufrufs: LangChain4j baut aus diesem Record das
 * Schema, das das Modell ausfuellen muss - und die enums {@link Prioritaet} und
 * {@link Kategorie} liefern dabei die erlaubten Werte.
 */
public record Triage(String titel,
                     Prioritaet prioritaet,
                     Kategorie kategorie,
                     boolean rueckrufNoetig,
                     String begruendung) {
}
