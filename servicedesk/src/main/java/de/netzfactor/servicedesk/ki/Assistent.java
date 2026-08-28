package de.netzfactor.servicedesk.ki;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Der Assistent im ServiceDesk: er darf nachschlagen, rechnen und schreiben.
 *
 * <p>Den {@link Handbuchsucher} zieht die Erweiterung von selbst heran, weil er
 * als CDI-Bean vom Typ RetrievalAugmentor im Projekt liegt.
 *
 * <p>Die Werkzeuge kommen aus zwei Quellen: {@link ToolBox} nennt die eigenen
 * Klassen dieser Anwendung, {@link McpToolBox} die Werkzeuge fremder Systeme
 * ueber MCP. Deren Namen und Beschreibungen kennt diese Datei nicht - sie
 * werden beim Verbindungsaufbau erfragt. Kommt im Lagersystem ein Werkzeug
 * dazu, aendert sich hier nichts.
 *
 * <p><b>Die Falle, ueber die man genau einmal stolpert:</b> die eigenen
 * Werkzeuge stehen hier in {@code @ToolBox} an der Methode und <em>nicht</em>
 * in {@code @RegisterAiService(tools = ...)} an der Schnittstelle. Stehen sie
 * dort, wird der MCP-Werkzeugkasten <em>stillschweigend verworfen</em>: kein
 * Fehler, keine Warnung, das Modell bekommt die fremden Werkzeuge einfach nie
 * zu sehen und antwortet "weiss ich nicht". Beide Wege fuehren zu einem
 * ToolProvider, und es gibt nur einen davon je Dienst.
 */
@RegisterAiService
@ApplicationScoped
public interface Assistent {

    /**
     * Die drei Waechter laufen in dieser Reihenfolge, und die ist Absicht -
     * sortiert nach dem, was sie kosten. Der erste, der ablehnt, beendet die
     * Kette; die dahinter werden gar nicht mehr ausgefuehrt. (Vor allen dreien
     * laeuft allerdings noch die Handbuchsuche - siehe {@link Tokenwaechter}.)
     *
     * <p><b>Das setzt voraus, dass die Waechter {@code fatal(...)} zurueckgeben
     * und nicht {@code failure(...)}.</b> Ein failure wird nur gesammelt: die
     * Kette laeuft weiter, jeder folgende Waechter fragt sein Modell, und am
     * Ende gewinnt die zuletzt gesetzte Meldung. Man sieht das nicht am
     * Rueckgabewert, sondern nur im Protokoll - und dann steht dort zweimal
     * "abgelehnt" fuer eine einzige Frage.
     * <ol>
     *   <li>{@link Tokenwaechter} - eine Datenbankabfrage. Darf der Benutzer noch?</li>
     *   <li>{@link Inhaltswaechter} - ein Modellaufruf. Ist der Inhalt zulaessig?</li>
     *   <li>{@link Themenwaechter} - noch einer. Geht es ueberhaupt um IT-Tickets?</li>
     * </ol>
     */
    @InputGuardrails({ Tokenwaechter.class, Inhaltswaechter.class, Themenwaechter.class })
    @ToolBox({ Ticketwerkzeuge.class, Datenbankwerkzeug.class })
    @McpToolBox({ "lager", "wissen" })
    @SystemMessage("""
            Du bist der Assistent im ServiceDesk eines IT-Dienstleisters.
            Du antwortest immer auf Deutsch, kurz und sachlich.

            Deine Werkzeuge:
            - ticketNachschlagen: sobald in der Frage eine Kennung der Form S-0007 vorkommt.
            - ticketAnlegen, prioritaetSetzen und kommentieren: nur, wenn der Benutzer
              es ausdrücklich verlangt. Fehlt für ein neues Ticket die Firma oder der
              Melder, fragst du einmal nach, statt etwas zu erfinden.
            - abfragen: für jede Frage nach Zahlen, Mengen, Summen oder Ranglisten.
            - teilNachschlagen, teileSuchen und knappeTeile: für alles, was das
              Ersatzteillager betrifft. Diese drei laufen im Lagersystem, nicht hier.
              Teilenummern haben die Form T-1007.
            - microsoft_docs_search: für Störungen an Windows, Office, Outlook, Teams,
              Entra ID oder Azure - besonders bei Fehlercodes wie 0x8004010F. Nennst du
              eine Lösung von dort, gibst du die Quelle an. Was du dort nicht findest,
              erfindest du nicht.

            Für "abfragen" schreibst du genau eine lesende SQL-Abfrage für PostgreSQL.
            Das Schema lautet:
            ticket(id, kennung, titel, beschreibung, firma, melder, kategorie,
                   prioritaet, status, bearbeiter, gemeldet_am, erledigt_am)
            kommentar(id, text, autor, geschrieben_am, ticket_id)
            zeitbuchung(id, minuten, bearbeiter, ticket_id)
            kategorie: ZUGANG, NETZ, DRUCKER, HARDWARE, SOFTWARE, SONSTIGES
            prioritaet: NIEDRIG, NORMAL, HOCH, KRITISCH
            status: NEU, IN_ARBEIT, WARTET, ERLEDIGT
            Firma, Melder, Kategorie und Bearbeiter stehen als Text in der Tabelle ticket
            selbst - fuer Fragen nach Firma, Kategorie oder Bearbeiter ist kein JOIN noetig.

            Jede Zahl in deiner Antwort stammt aus einer solchen Abfrage, niemals aus deiner Erinnerung.
            Hast du abgefragt, nennst du die benutzte Abfrage am Ende in einer eigenen Zeile:
            Abfrage: SELECT ...

            Wird eine Abfrage abgelehnt, liest du die Ablehnung und versuchst es einmal anders.
            Was weder in der Datenbank noch im Handbuch steht, beantwortest du mit "weiss ich nicht".
            Erfinde nichts.
            """)
    String frage(@MemoryId String sitzung, @UserMessage String frage);
}
