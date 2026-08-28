package de.netzfactor.servicedesk.domain;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tickets und Zeitbuchungen per JDBC - der Lader fuer die Bloecke vor Quarkus.
 */
public final class Datenbank {

    // Der Handbetrieb: in Block 12 faellt diese Klasse ersatzlos weg, weil Panache das uebernimmt.
    private static final String URL = "jdbc:postgresql://localhost:5433/servicedesk";
    private static final String OHNE = "Keine Verbindung zur Datenbank. Laeuft 'docker compose up -d'?";

    private Datenbank() {
    }

    public static List<Ticket> tickets() {
        List<Ticket> tickets = new ArrayList<>();
        try (Connection verbindung = verbinde();
             PreparedStatement abfrage =
                     verbindung.prepareStatement("select * from ticket order by gemeldet_am desc");
             ResultSet zeile = abfrage.executeQuery()) {
            while (zeile.next()) {
                Ticket ticket = new Ticket();
                ticket.kennung = zeile.getString("kennung");
                ticket.titel = zeile.getString("titel");
                ticket.beschreibung = zeile.getString("beschreibung");
                ticket.firma = zeile.getString("firma");
                ticket.melder = zeile.getString("melder");
                ticket.bearbeiter = zeile.getString("bearbeiter");
                ticket.kategorie = Kategorie.valueOf(zeile.getString("kategorie"));
                ticket.prioritaet = Prioritaet.valueOf(zeile.getString("prioritaet"));
                ticket.status = Status.valueOf(zeile.getString("status"));
                ticket.gemeldetAm = zeitpunkt(zeile.getTimestamp("gemeldet_am"));
                ticket.erledigtAm = zeitpunkt(zeile.getTimestamp("erledigt_am"));
                tickets.add(ticket);
            }
        } catch (SQLException fehler) {
            throw new IllegalStateException(OHNE, fehler);
        }
        return tickets;
    }

    public static List<Zeitbuchung> zeitbuchungen() {
        List<Zeitbuchung> buchungen = new ArrayList<>();
        try (Connection verbindung = verbinde();
             PreparedStatement abfrage =
                     verbindung.prepareStatement("select minuten, bearbeiter from zeitbuchung");
             ResultSet zeile = abfrage.executeQuery()) {
            while (zeile.next()) {
                Zeitbuchung buchung = new Zeitbuchung();
                buchung.minuten = zeile.getInt("minuten");
                buchung.bearbeiter = zeile.getString("bearbeiter");
                buchungen.add(buchung);
            }
        } catch (SQLException fehler) {
            throw new IllegalStateException(OHNE, fehler);
        }
        return buchungen;
    }

    private static Connection verbinde() throws SQLException {
        return DriverManager.getConnection(URL, "servicedesk", "servicedesk");
    }

    private static LocalDateTime zeitpunkt(Timestamp wert) {
        return wert == null ? null : wert.toLocalDateTime();
    }
}
