package de.netzfactor.servicedesk.ki;

import dev.langchain4j.agent.tool.Tool;
import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Das Werkzeug, mit dem der Assistent selbst Zahlen aus der Datenbank holt - unter Aufsicht. */
@ApplicationScoped
public class Datenbankwerkzeug {

    public static final String SCHEMA = """
            ticket(id, kennung, titel, beschreibung, firma, melder, kategorie,
                   prioritaet, status, bearbeiter, gemeldet_am, erledigt_am)
            kommentar(id, text, autor, geschrieben_am, ticket_id)
            zeitbuchung(id, minuten, bearbeiter, ticket_id)
            kategorie: ZUGANG, NETZ, DRUCKER, HARDWARE, SOFTWARE, SONSTIGES
            prioritaet: NIEDRIG, NORMAL, HOCH, KRITISCH
            status: NEU, IN_ARBEIT, WARTET, ERLEDIGT
            Firma, Melder, Kategorie und Bearbeiter stehen als Text in der Tabelle ticket
            selbst - fuer Fragen nach Firma, Kategorie oder Bearbeiter ist kein JOIN noetig.
            """;

    private static final List<String> ERLAUBTE_TABELLEN =
            List.of("ticket", "kommentar", "zeitbuchung");

    private static final Pattern VERBOTENES_WORT = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke|copy|call|do|vacuum)\\b");

    private static final Pattern TABELLE = Pattern.compile("\\b(?:from|join)\\s+([a-z_][a-z0-9_]*)");

    private final AgroalDataSource datenquelle;

    public Datenbankwerkzeug(AgroalDataSource datenquelle) {
        this.datenquelle = datenquelle;
    }

    @Tool("Beantwortet Zahlenfragen ueber die Ticketdatenbank mit genau EINER lesenden SQL-Abfrage. Nur SELECT.")
    public String abfragen(String sql) {
        String abfrage = sql.trim();
        if (abfrage.endsWith(";")) {
            abfrage = abfrage.substring(0, abfrage.length() - 1).trim();
        }
        String klein = abfrage.toLowerCase(Locale.ROOT);

        // Was aus dem Modell kommt, ist geraten und nicht geprueft: ohne diese Pruefungen haette ein Satz im Chatfenster Schreibrechte auf der Datenbank.
        if (!klein.startsWith("select")) {
            return "Abgelehnt: erlaubt ist nur eine lesende Abfrage, die mit SELECT beginnt.";
        }
        if (klein.contains(";")) {
            return "Abgelehnt: mehrere Anweisungen in einer Abfrage sind nicht erlaubt.";
        }
        Matcher verboten = VERBOTENES_WORT.matcher(klein);
        if (verboten.find()) {
            return "Abgelehnt: das Schluesselwort " + verboten.group(1) + " ist nicht erlaubt.";
        }
        Matcher tabelle = TABELLE.matcher(klein);
        while (tabelle.find()) {
            if (!ERLAUBTE_TABELLEN.contains(tabelle.group(1))) {
                return "Abgelehnt: auf die Tabelle " + tabelle.group(1) + " darf nicht zugegriffen werden. "
                        + "Erlaubt sind: " + String.join(", ", ERLAUBTE_TABELLEN) + ".";
            }
        }
        // Das erzwungene LIMIT ist wichtiger als eine schoene Meldung: eine vergessene Grenze zieht die halbe Datenbank in den Kontext des Modells und kostet echtes Geld.
        if (!klein.contains("limit")) {
            abfrage = abfrage + " LIMIT 50";
        }

        return ausfuehren(abfrage);
    }

    private String ausfuehren(String abfrage) {
        try (Connection verbindung = datenquelle.getConnection();
             Statement anweisung = verbindung.createStatement();
             ResultSet ergebnis = anweisung.executeQuery(abfrage)) {

            ResultSetMetaData kopf = ergebnis.getMetaData();
            int spalten = kopf.getColumnCount();

            StringBuilder text = new StringBuilder();
            for (int spalte = 1; spalte <= spalten; spalte++) {
                text.append(spalte > 1 ? " | " : "").append(kopf.getColumnLabel(spalte));
            }

            int zeilen = 0;
            while (zeilen < 50 && ergebnis.next()) {
                text.append('\n');
                for (int spalte = 1; spalte <= spalten; spalte++) {
                    text.append(spalte > 1 ? " | " : "").append(ergebnis.getString(spalte));
                }
                zeilen++;
            }
            return zeilen == 0 ? text + "\n(keine Zeilen)" : text.toString();

        } catch (SQLException fehler) {
            return "Die Abfrage ist fehlgeschlagen: " + fehler.getMessage();
        }
    }
}
