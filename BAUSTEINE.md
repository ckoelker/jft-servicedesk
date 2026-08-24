# Bausteine

Hier steht alles, was du beim Bauen des ServiceDesk **kopierst** statt es zu
tippen: Feld-für-Feld-Zuweisungen aus einem `ResultSet`, Bibliotheksaufrufe mit
zwanzig Parametern, HTML, Sprachdateien. Was hier **nicht** steht, ist Absicht —
der Kern jedes Blocks wird getippt, denn genau darum geht es in dem Block. Die
Blocknummer in jeder Überschrift sagt dir, wann du den Abschnitt brauchst; alle
Klassen liegen im Paket `de.netzfactor.servicedesk`.

---

## B06 — Die drei Klassen des Datenmodells

Drei Dateien im Paket `de.netzfactor.servicedesk`: `Ticket.java`,
`Kommentar.java` und `Zeitbuchung.java` — gewöhnliche Klassen mit public fields,
noch ohne jede Annotation. In Block 12 kommen `@Entity`, `extends Basis` und die
JPA-Annotationen dazu; die Felder, die du hier anlegst, bleiben unverändert
stehen.

```java
package de.netzfactor.servicedesk;

import java.time.LocalDateTime;

/**
 * Der Vorgang, um den sich alles dreht.
 *
 * <p>Firma, Melder und Bearbeiter stehen als Werte hier, nicht als Verweis auf
 * eine Stammdatentabelle - fuer diese Woche waeren das drei Joins ohne
 * Erkenntnisgewinn.
 */
public class Ticket {

    public String kennung;
    public String titel;
    public String beschreibung;
    public String firma;
    public String melder;
    public Kategorie kategorie;
    public Prioritaet prioritaet;
    public Status status;

    /** Leer, solange niemand zustaendig ist - der haeufigste Grund fuer eine gerissene Zusage. */
    public String bearbeiter;

    public LocalDateTime gemeldetAm;
    public LocalDateTime erledigtAm;
}
```

```java
package de.netzfactor.servicedesk;

import java.time.LocalDateTime;

/** Ein Eintrag im Verlauf eines Tickets. */
public class Kommentar {

    public String text;
    public String autor;
    public LocalDateTime geschriebenAm;

    // Nur diese Richtung: eine Rueckliste am Ticket brauchte niemand, waere aber
    // die erste Stelle, an der versehentlich alles nachgeladen wird.
    public Ticket ticket;
}
```

```java
package de.netzfactor.servicedesk;

/** Gebuchte Arbeitszeit - die Grundlage des Auslastungsberichts. */
public class Zeitbuchung {

    public int minuten;
    public String bearbeiter;

    public Ticket ticket;
}
```

---

## B06 — Der Handbetrieb: `Datenbank.java`

Nach `Datenbank.java`, gleiches Paket. Zwölf Felder einzeln aus einem
`ResultSet` zu holen lehrt nichts — und in Block 12 fliegt die ganze Klasse
ersatzlos wieder raus, weil Panache die Arbeit übernimmt.

```java
package de.netzfactor.servicedesk;

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
```

---

## B06 — Die Testdatei `meldungen.csv`

Nach `src/main/resources/meldungen.csv`. Drei der zehn Zeilen sind absichtlich
kaputt: eine unbekannte Priorität (`DRINGEND`), eine Zeile mit drei statt vier
Feldern und eine mit leerem Titel.

```csv
firma;melder;titel;prioritaet
Nordlicht Werften GmbH;Anke Brüggemann;Kran 3 meldet Störcode E17;HOCH
Stadtwerke Aurich;Jens Ohlsen;Zählerauslesung bricht nachts ab;NORMAL
Kontor Sued AG;Marit Voss;Rechnungslauf hängt in der Freigabe;DRINGEND
Praxis Dr. Hansen;Silke Hansen;Kartenlesegerät ohne Verbindung;KRITISCH
Vos Logistik KG;Timo Vos;Tourenplanung lädt keine Karten;NORMAL
Nordlicht Werften GmbH;Anke Brüggemann;Ersatzteilsuche dauert zu lange
Stadtwerke Aurich;Ole Frerichs;Portal meldet Wartung, obwohl offen;NIEDRIG
Kontor Sued AG;Marit Voss;;HOCH
Praxis Dr. Hansen;Silke Hansen;Terminkalender druckt jeden Termin doppelt;NIEDRIG
Vos Logistik KG;Timo Vos;Barcodescanner verliert die Kopplung;HOCH
```

---

## B09 — Der Excel- und der PDF-Schreiber

Nach `Berichtsschreiber.java`: der Importblock der Datei sowie `alsExcel`,
`alsPdf` und die beiden Hilfsmethoden, die nur diese zwei brauchen. Das sind
Bibliotheksaufrufe mit vielen Parametern und ohne Erkenntnisgewinn. `kopf`,
`zeilen` und `alsText` fehlen hier mit Absicht — das ist der Reflection-Teil,
den du tippst.

```java
package de.netzfactor.servicedesk;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

    public static byte[] alsExcel(String titel, List<?> daten) {
        try (XSSFWorkbook mappe = new XSSFWorkbook();
             ByteArrayOutputStream strom = new ByteArrayOutputStream()) {

            XSSFSheet blatt = mappe.createSheet(titel);
            XSSFFont fett = mappe.createFont();
            fett.setBold(true);
            XSSFCellStyle kopfstil = mappe.createCellStyle();
            kopfstil.setFont(fett);

            List<String> kopf = daten.isEmpty() ? List.of() : kopf(daten.get(0).getClass());
            XSSFRow kopfzeile = blatt.createRow(0);
            for (int spalte = 0; spalte < kopf.size(); spalte++) {
                XSSFCell zelle = kopfzeile.createCell(spalte);
                zelle.setCellValue(kopf.get(spalte));
                zelle.setCellStyle(kopfstil);
            }

            List<List<Object>> zeilen = zeilen(daten);
            for (int nummer = 0; nummer < zeilen.size(); nummer++) {
                XSSFRow zeile = blatt.createRow(nummer + 1);
                List<Object> werte = zeilen.get(nummer);
                for (int spalte = 0; spalte < werte.size(); spalte++) {
                    fuelle(zeile.createCell(spalte), werte.get(spalte));
                }
            }
            for (int spalte = 0; spalte < kopf.size(); spalte++) {
                blatt.autoSizeColumn(spalte);
            }

            mappe.write(strom);
            return strom.toByteArray();
        } catch (IOException fehler) {
            throw new IllegalStateException("Excel-Bericht '" + titel + "' fehlgeschlagen", fehler);
        }
    }

    public static byte[] alsPdf(String titel, List<?> daten) {
        ByteArrayOutputStream strom = new ByteArrayOutputStream();
        Document dokument = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        try {
            PdfWriter.getInstance(dokument, strom);
            dokument.open();
            dokument.add(new Paragraph(titel, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            dokument.add(new Paragraph(" "));

            if (!daten.isEmpty()) {
                Font fett = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
                Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
                List<String> kopf = kopf(daten.get(0).getClass());

                PdfPTable tabelle = new PdfPTable(kopf.size());
                tabelle.setWidthPercentage(100);
                for (String ueberschrift : kopf) {
                    tabelle.addCell(new PdfPCell(new Phrase(ueberschrift, fett)));
                }
                for (List<Object> zeile : zeilen(daten)) {
                    for (Object wert : zeile) {
                        tabelle.addCell(new PdfPCell(new Phrase(text(wert), normal)));
                    }
                }
                dokument.add(tabelle);
            }
            dokument.close();
            return strom.toByteArray();
        } catch (DocumentException fehler) {
            throw new IllegalStateException("PDF-Bericht '" + titel + "' fehlgeschlagen", fehler);
        }
    }

    private static void fuelle(XSSFCell zelle, Object wert) {
        // Zahlen als Zahlen: sonst steht in Excel ein Text, mit dem sich nicht rechnen laesst.
        if (wert instanceof Number zahl) {
            zelle.setCellValue(zahl.doubleValue());
        } else if (wert != null) {
            zelle.setCellValue(String.valueOf(wert));
        }
    }

    private static String text(Object wert) {
        if (wert == null) {
            return "";
        }
        if (wert instanceof Double zahl) {
            return String.format(Locale.GERMANY, "%.1f", zahl);
        }
        return String.valueOf(wert);
    }
```

---

## B10 — Die Auskunft des Lagersystems

Zwei Dateien: `Lagerauskunft.java` und `Teil.java`. Vier Werte per regulärem
Ausdruck aus einem flachen JSON zu ziehen ist Fleißarbeit; interessant ist
etwas anderes — die Klasse **blockiert** bei jedem Aufruf, und genau das wird in
Block 15 zum Thema.

```java
package de.netzfactor.servicedesk;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Die Auskunft des Lagersystems - ein blockierender Aufruf je Teil.
 *
 * <p>Die Klasse ist absichtlich weder final noch statisch: so laesst sie sich
 * im Test durch ein Mockito-Mock ersetzen, ohne dass am Quelltext etwas
 * geaendert werden muss.
 */
@ApplicationScoped
public class Lagerauskunft {

    private static final String BASIS = "http://localhost:8082";

    // Vier Felder aus einem flachen JSON - fuer diesen einen Zweck ist ein
    // Muster ehrlicher als ein Objektmodell samt Abhaengigkeit.
    private static final Pattern NUMMER = Pattern.compile("\"nummer\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern BEZEICHNUNG = Pattern.compile("\"bezeichnung\"\\s*:\\s*\"([^\"]*)\"");
    private static final Pattern BESTAND = Pattern.compile("\"bestand\"\\s*:\\s*(-?\\d+)");
    private static final Pattern LAGERORT = Pattern.compile("\"lagerort\"\\s*:\\s*\"([^\"]*)\"");

    private final String basis;
    private final HttpClient klient = HttpClient.newHttpClient();

    public Lagerauskunft() {
        this(BASIS);
    }

    public Lagerauskunft(String basis) {
        this.basis = basis;
    }

    public Teil nach(String nummer) {
        try {
            HttpRequest anfrage = HttpRequest.newBuilder()
                    .uri(URI.create(basis + "/teile/" + nummer))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            HttpResponse<String> antwort = klient.send(anfrage, HttpResponse.BodyHandlers.ofString());
            if (antwort.statusCode() != 200) {
                throw new IllegalStateException(
                        "Lagersystem meldet für " + nummer + " den Status " + antwort.statusCode());
            }
            String json = antwort.body();
            return new Teil(
                    wert(json, NUMMER, "nummer"),
                    wert(json, BEZEICHNUNG, "bezeichnung"),
                    Integer.parseInt(wert(json, BESTAND, "bestand")),
                    wert(json, LAGERORT, "lagerort"));
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Abruf von " + nummer + " wurde unterbrochen", fehler);
        } catch (IOException fehler) {
            throw new IllegalStateException("Lagersystem nicht erreichbar: " + basis, fehler);
        }
    }

    private static String wert(String json, Pattern muster, String feld) {
        Matcher treffer = muster.matcher(json);
        if (!treffer.find()) {
            throw new IllegalStateException("Feld " + feld + " fehlt in der Antwort: " + json);
        }
        return treffer.group(1);
    }
}
```

```java
package de.netzfactor.servicedesk;

/** Ein Ersatzteil, so wie das Lagersystem es liefert. */
public record Teil(String nummer, String bezeichnung, int bestand, String lagerort) {
}
```

---

## B12 — `Basis` und die Annotationen

Zuerst die neue Datei `Basis.java` — der Schlüssel, den sich alle drei Entities
teilen.

```java
package de.netzfactor.servicedesk;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Der Schluessel, den alle drei Entities gemeinsam haben.
 *
 * <p>IDENTITY, weil die Spalten in der Datenbank <code>bigserial</code> sind -
 * die Nummer vergibt Postgres, nicht Hibernate.
 */
@MappedSuperclass
public abstract class Basis extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
}
```

Danach bekommen die drei Klassen aus Block 6 ihre Annotationen. Es entsteht
keine neue Datei: Du ergänzt oben die Importe, setzt `@Entity` und
`extends Basis` an die Klasse und schreibst die Annotationen vor die betroffenen
Felder. Alle Felder bleiben, wie sie sind — den Schlüssel `id` bekommen die drei
jetzt von `Basis`. Die statischen Abfragen darunter tippst du selbst, das ist
der Punkt des Blocks.

`Ticket.java`:

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Ticket extends Basis {

    @Column(length = 2000)
    public String beschreibung;

    @Enumerated(EnumType.STRING)
    public Kategorie kategorie;

    @Enumerated(EnumType.STRING)
    public Prioritaet prioritaet;

    @Enumerated(EnumType.STRING)
    public Status status;
```

`Kommentar.java`:

```java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class Kommentar extends Basis {

    @Column(length = 2000)
    public String text;

    @ManyToOne(fetch = FetchType.LAZY)
    public Ticket ticket;
```

`Zeitbuchung.java`:

```java
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class Zeitbuchung extends Basis {

    @ManyToOne(fetch = FetchType.LAZY)
    public Ticket ticket;
```

---

## B13 — Drei der fünf Tests

Nach `src/test/java/de/netzfactor/servicedesk/`. Diese drei sind Fleissarbeit:
lange Zusicherungsketten, ein JSON-Rumpf, ein Mock-Aufbau. `SlaTest` und
`MeldungsimportTest` tippst du selbst — an ihnen sieht man, worum es geht: ein
Lambda statt eines Mocks, und ein Ergebnistyp statt einer Exception.

```java
package de.netzfactor.servicedesk;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Derselbe Schreiber fuer jeden Bericht - er liest die Spalten zur Laufzeit vom record ab. */
class BerichtsschreiberTest {

    private static final List<Zeilen.Auslastung> DATEN = List.of(
            new Zeilen.Auslastung("Anke Brehm", 3L, 7.5),
            new Zeilen.Auslastung("Bernd Kappel", 1L, 2.0));

    @Test
    void derKopfKommtAusDenSpaltenAnnotationenInReihenfolgeDerKomponenten() {
        assertThat(Berichtsschreiber.kopf(Zeilen.Auslastung.class))
                .containsExactly("Bearbeiter", "Tickets", "Stunden");
    }

    @Test
    void dieZeilenLiefernDieWerteInDerselbenReihenfolge() {
        assertThat(Berichtsschreiber.zeilen(DATEN))
                .containsExactly(List.of("Anke Brehm", 3L, 7.5),
                                 List.of("Bernd Kappel", 1L, 2.0));
    }

    @Test
    void derTextberichtZeigtUeberschriftUndWerte() {
        assertThat(Berichtsschreiber.alsText("Auslastung der Bearbeiter", DATEN))
                .contains("Auslastung der Bearbeiter")
                .contains("Bearbeiter")
                .contains("Anke Brehm");
    }

    @Test
    void excelUndPdfTragenIhreSignaturAmAnfang() {
        // xlsx ist ein ZIP, deshalb PK; ein PDF beginnt immer mit %PDF.
        assertThat(Berichtsschreiber.alsExcel("Auslastung", DATEN))
                .startsWith((byte) 'P', (byte) 'K');
        assertThat(Berichtsschreiber.alsPdf("Auslastung", DATEN))
                .startsWith((byte) '%', (byte) 'P', (byte) 'D', (byte) 'F');
    }
}
```

```java
package de.netzfactor.servicedesk;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Das Lagersystem im Test durch ein Mock ersetzt - der Test laeuft ohne HTTP. */
class LagerauskunftTest {

    // Ein Fremdsystem im Test kostet Zeit und faellt aus; das Mock antwortet immer gleich.
    private final Lagerauskunft auskunft = Mockito.mock(Lagerauskunft.class);

    @Test
    void dasMockAntwortetStattDesLagersystems() {
        Mockito.when(auskunft.nach("T-1001"))
               .thenReturn(new Teil("T-1001", "Netzteil 150 W", 3, "Regal A3"));

        assertThat(auskunft.nach("T-1001").bestand()).isEqualTo(3);
        Mockito.verify(auskunft, Mockito.times(1)).nach("T-1001");
        Mockito.verifyNoMoreInteractions(auskunft);
    }

    @Test
    void beimAusfallDesLagersystemsBekommtDerAufruferDenGrundZuSehen() {
        Mockito.when(auskunft.nach("T-9999"))
               .thenThrow(new IllegalStateException("Lagersystem nicht erreichbar: http://localhost:8082"));

        assertThatThrownBy(() -> auskunft.nach("T-9999"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht erreichbar");
    }
}
```

```java
package de.netzfactor.servicedesk;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/** Der ganze Weg eines Tickets ueber HTTP - gegen die echte Datenbank. */
@QuarkusTest
class TicketRessourceTest {

    private static final String TITEL = "VPN bricht nach zehn Minuten ab";

    // Firma und Melder stehen als Werte am Ticket, deshalb reicht der Rumpf ohne Stammdaten.
    private static final String NEUES_TICKET = """
            {"titel": "VPN bricht nach zehn Minuten ab",
             "beschreibung": "Nur fuer den Test angelegt.",
             "prioritaet": "HOCH",
             "kategorie": "NETZ",
             "firma": "Nordlicht Werften GmbH",
             "melder": "Anke Brehm"}
            """;

    @Test
    void einTicketWirdAngelegtGelesenErledigtKommentiertUndWiederGeloescht() {
        String kennung = given().contentType(ContentType.JSON).body(NEUES_TICKET)
                .when().post("/tickets")
                .then().statusCode(201)
                .extract().path("kennung");

        String titel = given()
                .when().get("/tickets/{kennung}", kennung)
                .then().statusCode(200)
                .extract().path("titel");
        assertThat(titel).isEqualTo(TITEL);

        JsonPath erledigt = given()
                .when().put("/tickets/{kennung}/status/ERLEDIGT", kennung)
                .then().statusCode(200)
                .extract().jsonPath();
        assertThat(erledigt.getString("status")).isEqualTo("ERLEDIGT");
        assertThat(erledigt.getString("erledigtAm")).isNotNull();

        given().contentType(ContentType.JSON)
                .body("""
                      {"text": "Router getauscht, laeuft wieder.", "autor": "Bernd Kappel"}
                      """)
                .when().post("/tickets/{kennung}/kommentare", kennung)
                .then().statusCode(201);

        List<String> autoren = given()
                .when().get("/tickets/{kennung}/kommentare", kennung)
                .then().statusCode(200)
                .extract().jsonPath().getList("autor", String.class);
        assertThat(autoren).containsExactly("Bernd Kappel");

        given().when().delete("/tickets/{kennung}", kennung).then().statusCode(204);
        given().when().get("/tickets/{kennung}", kennung).then().statusCode(404);
    }

    @Test
    void einTicketOhneTitelWirdAbgelehnt() {
        given().contentType(ContentType.JSON)
                .body(NEUES_TICKET.replace(TITEL, ""))
                .when().post("/tickets")
                .then().statusCode(400);
    }
}
```

---

## B17 — Der System-Prompt und das Tabellenschema

Der `@SystemMessage`-Textblock gehört in `Assistent.java`, direkt über die
Methode `frage`. So genau muss ein System-Prompt sein — abtippen kostet nur
Zeit.

```java
    @SystemMessage("""
            Du bist der Assistent im ServiceDesk eines IT-Dienstleisters.
            Du antwortest immer auf Deutsch, kurz und sachlich.

            Deine Werkzeuge:
            - ticketNachschlagen: sobald in der Frage eine Kennung der Form S-0007 vorkommt.
            - ticketAnlegen, prioritaetSetzen und kommentieren: nur, wenn der Benutzer
              es ausdrücklich verlangt. Fehlt für ein neues Ticket die Firma oder der
              Melder, fragst du einmal nach, statt etwas zu erfinden.
            - abfragen: für jede Frage nach Zahlen, Mengen, Summen oder Ranglisten.

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
```

```java
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
```


## B18 — Die beiden Qute-Vorlagen

Nach `src/main/resources/templates/Seiten/tickets.html` — die ganze Seite samt
CSS und den beiden kleinen Skripten für Chat und Event-Stream.

```html
<!DOCTYPE html>
<html lang="{beschriftung.sprache}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{beschriftung.titel}</title>
<style>{|
body { font-family: system-ui, "Segoe UI", Arial, sans-serif; margin: 0;
       color: #23282d; background: #f6f7f9; }
.rahmen { max-width: 1100px; margin: 0 auto; padding: 24px 16px 48px; }

h1 { margin: 0 0 4px; font-size: 26px; }
h2 { margin: 0 0 8px; font-size: 16px; }
.unter { margin: 0; color: #6b7280; }
.zahlen { margin: 12px 0 0; font-size: 14px; color: #6b7280; }

.strom { margin: 4px 0 20px; font-size: 13px; color: #6b7280; }
.ampel { display: inline-block; width: 8px; height: 8px; border-radius: 50%;
         margin-right: 6px; background: #b9bec4; }
.ampel.an { background: #3f9b4f; }
.ampel.aus { background: #d64545; }

.raster { display: grid; grid-template-columns: 1fr 300px; gap: 24px;
          align-items: start; }
@media (max-width: 900px) { .raster { grid-template-columns: 1fr; } }

.neu { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px;
       background: #fff; border: 1px solid #e3e6e9; border-radius: 6px; padding: 10px; }
.neu input, .neu select { font: inherit; font-size: 13px; padding: 5px 8px;
       border: 1px solid #ccd1d6; border-radius: 4px; }
.neu input[name="betreff"] { flex: 1 1 260px; }
.neu button { font-weight: 600; }

.filter { margin-bottom: 12px; }
.filter button { margin-right: 6px; }
.bericht { margin-left: 18px; font-size: 13px; color: #5b636b; }
.bericht a { margin-left: 6px; }

button { font: inherit; font-size: 13px; padding: 5px 12px; cursor: pointer;
         border: 1px solid #ccd1d6; border-radius: 4px; background: #fff; }
button:hover { border-color: #8f979f; }
button.weg:hover { border-color: #d64545; color: #d64545; }

table { width: 100%; border-collapse: collapse; background: #fff;
        border: 1px solid #e3e6e9; border-radius: 6px; font-size: 14px; }
th { text-align: left; font-weight: 600; color: #6b7280; font-size: 12px;
     text-transform: uppercase; letter-spacing: .04em; }
th, td { padding: 9px 12px; border-bottom: 1px solid #eceff1; }
tr:last-child td { border-bottom: none; }
.kennung { font-family: ui-monospace, Consolas, monospace; white-space: nowrap; }
.datum { white-space: nowrap; }
.aktionen { white-space: nowrap; }
.aktionen button { padding: 3px 8px; margin-left: 3px; }

.punkt { display: inline-block; width: 9px; height: 9px; border-radius: 50%;
         margin-right: 7px; background: #b9bec4; }
.prio-KRITISCH { background: #d64545; }
.prio-HOCH { background: #e08a1e; }
.prio-NORMAL { background: #2f6fb2; }
.prio-NIEDRIG { background: #9aa1a8; }

aside { background: #fff; border: 1px solid #e3e6e9; border-radius: 6px;
        padding: 14px; }
textarea { width: 100%; box-sizing: border-box; font: inherit; font-size: 14px;
           height: 90px; padding: 7px; border: 1px solid #ccd1d6;
           border-radius: 4px; resize: vertical; }
#antwort { margin-top: 12px; font-size: 14px; line-height: 1.5;
           white-space: pre-wrap; }
|}</style>
<script src="/htmx.min.js"></script>
</head>

<body data-denkt="{beschriftung.chatDenkt}"
      data-verbunden="{beschriftung.stromVerbunden}"
      data-getrennt="{beschriftung.stromGetrennt}">
<div class="rahmen">

  <header>
    <h1>{beschriftung.titel}</h1>
    <p class="unter">{beschriftung.untertitel}</p>
    <p class="zahlen">{beschriftung.zusammenfassung}</p>
    <p class="strom"><span id="ampel" class="ampel"></span><span id="stromtext"></span></p>
  </header>

  <div class="raster">
    <main>
      <form class="neu" hx-post="/aktion/neu" hx-target="#zeilen" hx-swap="innerHTML"
            hx-on::after-request="this.reset()">
        <input name="betreff" placeholder="{beschriftung.neuBetreff}" required>
        <input name="firma" placeholder="{beschriftung.neuFirma}" list="firmen">
        <input name="melder" placeholder="{beschriftung.neuMelder}">
        <select name="kategorie">
          {#for k in beschriftung.kategorien}<option value="{k.schluessel}">{k.text}</option>{/for}
        </select>
        <select name="prioritaet">
          {#for p in beschriftung.prioritaeten}<option value="{p.schluessel}">{p.text}</option>{/for}
        </select>
        <button type="submit">{beschriftung.neuAnlegen}</button>
      </form>

      <datalist id="firmen">
        <option value="Nordlicht Werften GmbH"></option>
        <option value="Stadtwerke Aurich"></option>
        <option value="Kontor Sued AG"></option>
        <option value="Praxis Dr. Hansen"></option>
        <option value="Vos Logistik KG"></option>
      </datalist>

      <div class="filter">
        <button data-filter="alle" hx-get="/teile?filter=alle"
                hx-target="#zeilen" hx-swap="innerHTML">{beschriftung.filterAlle}</button>
        <button data-filter="offen" hx-get="/teile?filter=offen"
                hx-target="#zeilen" hx-swap="innerHTML">{beschriftung.filterOffen}</button>
        <button data-filter="kritisch" hx-get="/teile?filter=kritisch"
                hx-target="#zeilen" hx-swap="innerHTML">{beschriftung.filterKritisch}</button>

        <span class="bericht">{beschriftung.berichtTitel}:
          <a href="/tickets/bericht.xlsx?art=sla">Excel</a>
          <a href="/tickets/bericht.pdf?art=sla">PDF</a>
        </span>
      </div>

      <table>
        <thead>
          <tr>
            <th>{beschriftung.spalteKennung}</th>
            <th>{beschriftung.spalteTitel}</th>
            <th>{beschriftung.spaltePrioritaet}</th>
            <th>{beschriftung.spalteStatus}</th>
            <th>{beschriftung.spalteKategorie}</th>
            <th>{beschriftung.spalteFirma}</th>
            <th>{beschriftung.spalteGemeldet}</th>
            <th>{beschriftung.spalteBearbeiter}</th>
            <th>{beschriftung.spalteAktion}</th>
          </tr>
        </thead>
        <!-- Dieselbe Vorlage wie das htmx-Fragment: der Zeilenaufbau steht nur einmal. -->
        <tbody id="zeilen">{#include Seiten/teile /}</tbody>
      </table>
    </main>

    <aside>
      <h2>{beschriftung.chatTitel}</h2>
      <textarea id="frage" placeholder="{beschriftung.chatPlatzhalter}"></textarea>
      <p><button id="senden">{beschriftung.chatSenden}</button></p>
      <div id="antwort"></div>
    </aside>
  </div>

</div>

<script>{|
var frage = document.getElementById('frage');
var antwort = document.getElementById('antwort');
var sitzung = 'web-' + Math.random().toString(36).slice(2);

document.getElementById('senden').addEventListener('click', function () {
  var text = frage.value.trim();
  if (!text) { return; }
  antwort.textContent = document.body.dataset.denkt;
  fetch('/assistent?sitzung=' + sitzung, {
    method: 'POST',
    headers: { 'Content-Type': 'text/plain' },
    body: text
  }).then(function (rueck) {
    return rueck.text();
  }).then(function (gesagt) {
    antwort.textContent = gesagt;
  }).catch(function (fehler) {
    antwort.textContent = String(fehler);
  });
});
|}</script>

<script>{|
var aktuellerFilter = 'alle';
var ampel = document.getElementById('ampel');
var stromtext = document.getElementById('stromtext');
stromtext.textContent = document.body.dataset.getrennt;

document.querySelectorAll('.filter button').forEach(function (knopf) {
  knopf.addEventListener('click', function () { aktuellerFilter = knopf.dataset.filter; });
});

// Der Filter haengt an der Seite, nicht am Knopf - die Aktionen bekommen ihn erst hier dazu.
document.body.addEventListener('htmx:configRequest', function (fall) {
  if (fall.detail.path.indexOf('/aktion/') === 0) {
    fall.detail.path += '?filter=' + aktuellerFilter;
  }
});

var strom = new EventSource('/tickets/strom');
strom.onopen = function () {
  ampel.className = 'ampel an';
  stromtext.textContent = document.body.dataset.verbunden;
};
strom.onerror = function () {
  ampel.className = 'ampel aus';
  stromtext.textContent = document.body.dataset.getrennt;
};
strom.onmessage = function () {
  htmx.ajax('GET', '/teile?filter=' + aktuellerFilter, '#zeilen');
};
|}</script>

</body>
</html>
```

```html
{#for zeile in zeilen}
<tr>
  <td class="kennung">{zeile.kennung}</td>
  <td>{zeile.titel}</td>
  <td><span class="punkt prio-{zeile.prioritaetSchluessel}"></span>{zeile.prioritaet}</td>
  <td>{zeile.status}</td>
  <td>{zeile.kategorie}</td>
  <td>{zeile.firma}</td>
  <td class="datum">{zeile.gemeldet}</td>
  <td>{zeile.bearbeiter}</td>
  <td class="aktionen">
    <button hx-post="/aktion/{zeile.kennung}/erledigt"
            hx-target="#zeilen" hx-swap="innerHTML">&#10003;</button>
    <button hx-post="/aktion/{zeile.kennung}/hoeher"
            hx-target="#zeilen" hx-swap="innerHTML">&#8593;</button>
    <button class="weg" hx-delete="/aktion/{zeile.kennung}"
            hx-target="#zeilen" hx-swap="innerHTML">&#10005;</button>
  </td>
</tr>
{/for}
```


## B18 — Die beiden Sprachdateien

Nach `src/main/resources/meldungen_de.properties` und
`src/main/resources/meldungen_en.properties`. Beide Dateien sind UTF-8.

```properties
# Die deutschen Texte der Oberflaeche. Datei ist UTF-8.

seite.titel=ServiceDesk
seite.untertitel=Alle Vorgänge auf einen Blick

spalte.kennung=Kennung
spalte.titel=Titel
spalte.prioritaet=Priorität
spalte.status=Status
spalte.kategorie=Kategorie
spalte.firma=Firma
spalte.gemeldet=Gemeldet
spalte.bearbeiter=Bearbeiter
spalte.aktion=Aktion

aktion.erledigt=Erledigt
aktion.hoeher=Höher stufen
aktion.loeschen=Löschen

filter.alle=Alle
filter.offen=Offen
filter.kritisch=Kritisch

zusammenfassung={0} Tickets, davon {1} offen

chat.titel=Assistent
chat.platzhalter=Frag etwas zu den Tickets ...
chat.senden=Fragen
chat.denkt=Der Assistent denkt nach ...

strom.verbunden=verbunden
strom.getrennt=getrennt

prioritaet.NIEDRIG=Niedrig
prioritaet.NORMAL=Normal
prioritaet.HOCH=Hoch
prioritaet.KRITISCH=Kritisch

status.NEU=Neu
status.IN_ARBEIT=In Arbeit
status.WARTET=Wartet
status.ERLEDIGT=Erledigt

kategorie.ZUGANG=Zugang
kategorie.NETZ=Netzwerk
kategorie.DRUCKER=Drucker
kategorie.HARDWARE=Hardware
kategorie.SOFTWARE=Software
kategorie.SONSTIGES=Sonstiges

niemand=niemand

# Der Bericht aus Block 9, jetzt als Download
bericht.titel=SLA-Bericht herunterladen

# Das Formular, mit dem eine Meldung hereinkommt
neu.titel=Neues Ticket
neu.betreff=Worum geht es?
neu.firma=Firma
neu.melder=Melder
neu.anlegen=Anlegen
```

```properties
# Die englischen Texte der Oberflaeche. Datei ist UTF-8.

seite.titel=ServiceDesk
seite.untertitel=Every case at a glance

spalte.kennung=Reference
spalte.titel=Subject
spalte.prioritaet=Priority
spalte.status=Status
spalte.kategorie=Category
spalte.firma=Company
spalte.gemeldet=Reported
spalte.bearbeiter=Assignee
spalte.aktion=Action

aktion.erledigt=Close
aktion.hoeher=Raise priority
aktion.loeschen=Delete

filter.alle=All
filter.offen=Open
filter.kritisch=Critical

zusammenfassung={0} tickets, {1} of them open

chat.titel=Assistant
chat.platzhalter=Ask something about the tickets ...
chat.senden=Ask
chat.denkt=The assistant is thinking ...

strom.verbunden=connected
strom.getrennt=disconnected

prioritaet.NIEDRIG=Low
prioritaet.NORMAL=Normal
prioritaet.HOCH=High
prioritaet.KRITISCH=Critical

status.NEU=New
status.IN_ARBEIT=In progress
status.WARTET=Waiting
status.ERLEDIGT=Closed

kategorie.ZUGANG=Access
kategorie.NETZ=Network
kategorie.DRUCKER=Printing
kategorie.HARDWARE=Hardware
kategorie.SOFTWARE=Software
kategorie.SONSTIGES=Other

niemand=nobody

# Der Bericht aus Block 9, jetzt als Download
bericht.titel=Download SLA report

# Das Formular, mit dem eine Meldung hereinkommt
neu.titel=New ticket
neu.betreff=What is the problem?
neu.firma=Company
neu.melder=Reported by
neu.anlegen=Create
```


## Was hier absichtlich fehlt

- **B06** — die Enums `Prioritaet`, `Status` und `Kategorie`
- **B06** — das sealed interface `Ergebnis` und `Meldungsimport`
- **B07** — `Kalender` und `Sla`
- **B08** — `Zeilen` und die drei Auswertungen in `Berichte`
- **B09** — die Annotation `@Spalte` und der Reflection-Teil des `Berichtsschreiber`
- **B10** — `Braucht`, `Container` und `Ersatzteilpruefer`
- **B11** — die REST-Ressourcen und die Ansichts-records
- **B12** — die Panache-Abfragen in den drei Entities
- **B13** — die fünf Tests
- **B14** — `Ticketstrom` und der Eskalationslauf
- **B15** — die Schleife mit den virtual threads
- **B16** — `Triagedienst` und der record `Triage`
- **B17** — die `@Tool`-Methoden
- **B18** — `Texte`, `Zeile`, `Beschriftung` und `Oberflaeche`

Das ist der Stoff, den du tippst.
