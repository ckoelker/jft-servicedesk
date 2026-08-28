# Die Anwendung im Ganzen

Diese Datei ist zum **Nachmachen zu Hause** gedacht: Sie beschreibt die fertige
Anwendung und führt die Vorführung vom Freitag Schritt für Schritt vor, mit
allen Befehlen und den Ausgaben, die dabei herauskommen sollen.

Wie du das Projekt startest, steht im [README](README.md) — Docker, Ports,
Anmeldedaten, OpenAI-Schlüssel. Hier geht es darum, was dabei passiert und
warum.

---

## Was hier eigentlich läuft

Ein kleines Ticketsystem für einen IT-Dienstleister. Es ist bewusst klein
gehalten: **fünf Tabellen, zwei Beziehungen, keine Stammdaten.** Drei davon
sind die Fachlichkeit — `ticket`, `kommentar`, `zeitbuchung` —, die beiden
anderen tragen den Betrieb: `benutzer` und `tokenkonto`.

Firma, Melder und Bearbeiter stehen als Text am Ticket, `Kategorie` ist ein
Java-`enum` mit den SLA-Stunden darin. Das ist der Grund, warum der Assistent
später Fragen nach Firma oder Kategorie **ohne JOIN** beantwortet.

Der Datenbestand ist fest und ohne Zufall aufgebaut: **60 Tickets, 150
Kommentare, 135 Zeitbuchungen**, davon **31 Tickets offen**. Jeder bekommt
exakt dieselben Zahlen — deshalb lässt sich jede Auswertung nachrechnen.

### Die sieben Pakete

Der Quelltext liegt nicht in einem Topf. Der Paketname sagt, wohin eine Datei
gehört:

| Paket | Was darin steht |
|---|---|
| `domain` | was ein Ticket **ist**: die drei Entities, die drei Aufzählungen, `Benutzer` und `Tokenkonto` |
| `dto` | was an der Außengrenze rein- und rausgeht |
| `de.netzfactor.servicedesk` | der Anwendungskern: was mit einem Ticket **passiert** |
| `auswertung` | was man aus den Tickets herausrechnet: SLA, Feiertage, Berichte |
| `lager` | das Fremdsystem und der Weg dorthin |
| `ki` | Modell, Werkzeuge, Handbuchsuche, die drei Wächter, die Tokenbuchhaltung |
| `web` | alles, was nach außen spricht: REST, Vorlagen, Sprachen, Anmeldung |

Die Abhängigkeiten laufen nur in eine Richtung — kein einziger Pfeil läuft
rückwärts:

```
domain      ->  (nichts)
lager       ->  (nichts)
dto         ->  domain
auswertung  ->  domain
Kern        ->  domain, dto
ki          ->  domain, dto, Kern
web         ->  alle
```

Das ist keine Kosmetik: Es ist der Grund, warum dieselbe SLA-Auswertung einmal
auf der Konsole und einmal über HTTP herauskommt, ohne dass eine Zeile davon
etwas von REST weiß.

---

## Alles starten

Drei Fenster. Die Reihenfolge ist wichtig, weil jedes auf das vorige wartet.

```bash
# 1 · Datenbank. Beim allerersten Start legt sie Schema und Daten selbst an.
docker compose up -d

# Kontrollabfrage: 60 Tickets müssen drinstehen.
docker exec servicedesk-db psql -U servicedesk -d servicedesk \
  -c "select count(*) from ticket"
#  count
# -------
#     60

# Und die beiden Anmeldungen, ohne die nichts geht.
docker exec servicedesk-db psql -U servicedesk -d servicedesk \
  -c "select benutzername, rollen from benutzer"
#  benutzername |        rollen
# --------------+-----------------------
#  mara         | bearbeiter,assistent
#  jonas        | bearbeiter
```

```bash
# 2 · Das Lagersystem — zweites Fenster. Es spricht auf demselben Port
#     zweierlei: REST und MCP.
./mvnw -pl lagersystem quarkus:dev

curl -s http://localhost:8082/teile/T-1001
# {"nummer":"T-1001","bezeichnung":"Tonerkassette 1","bestand":7,"lagerort":"Regal 2"}
```

```bash
# 3 · Der ServiceDesk — drittes Fenster. Der Schlüssel gehört in die Umgebung,
#     nicht in eine Datei, und in genau das Fenster, aus dem du startest.
export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY='sk-...'
./mvnw -pl servicedesk quarkus:dev
```

### Für zu Hause: ein eigener Schlüssel

Der Schlüssel aus dem Kurs war für den Kurs. **Für zu Hause legst du dir einen
eigenen an** — auf <https://platform.openai.com> unter *API keys*. Das
Guthaben wird getrennt abgerechnet, und ein Schlüssel, den sich mehrere teilen,
ist ohnehin keiner.

Was diese Anwendung verbraucht, ist überschaubar: Sie spricht `gpt-5.4-mini`
und `text-embedding-3-small` an — die kleinen Modelle, und zwar mit Absicht.
Zwei Bremsen sind schon eingebaut:

- **Das Gedächtnis** ist auf zwanzig Nachrichten je Sitzung begrenzt
  (`chat-memory.memory-window.max-messages`), sonst wächst jede Frage um den
  ganzen bisherigen Verlauf.
- **Das Tagesbudget** je Benutzer steht in `application.properties` unter
  `servicedesk.assistent.tokens-pro-tag`. Gebucht wird der Verbrauch, den das
  Modell tatsächlich meldet, nicht eine Schätzung — nachsehen kannst du in der
  Tabelle `tokenkonto`.

Der eine Schlüssel reicht für alles: Assistent, Handbuchsuche und beide Wächter
sprechen dasselbe Modell an. Und wenn du gar keinen anlegen willst — der
Abschnitt *Ohne OpenAI-Schlüssel* weiter unten zeigt, wie viel von der
Anwendung auch ohne läuft.

Beim Start steht im Protokoll eine Zeile der Form
`N Abschnitte aus 8 Handbuchdateien aufgenommen.` — dann hat die Aufnahme des
Handbuchs geklappt. Steht dort stattdessen `Kein OpenAI-Schluessel gesetzt` oder
`Kein Handbuch unter ...`, hilft die Tabelle unter *Wenn etwas klemmt*.

Danach liegt die Oberfläche auf <http://localhost:8080>.

---

## Die Vorführung, Schritt für Schritt

1. **Zwei Browserfenster nebeneinander**, beide auf <http://localhost:8080>.
   Es kommt die Anmeldeseite — in beiden Fenstern als `mara` / `mara` anmelden.
   Danach zeigen beide dieselbe Tabelle, oben ein grüner Punkt und `verbunden`.
   Das ist eine offene SSE-Verbindung, die von selbst nichts tut und gleich
   wichtig wird.

2. **Kurz ansehen, was da steht:** 60 Tickets, davon 31 offen. Die Spalte
   *Kategorie* kommt aus einem `enum`, die Spalte *Firma* steht als Wert am
   Ticket. Kein Join.

3. **Im linken Fenster** in den Chatkasten eingeben:
   *„Setz S-0007 auf KRITISCH."* Absenden.

4. **Auf das rechte Fenster schauen und nichts tun.** Nach ein paar Sekunden
   zieht dort die Zeile `S-0007` nach — der Punkt wird rot, die Priorität steht
   auf *Kritisch*. Niemand hat das rechte Fenster angefasst, niemand hat neu
   geladen.

5. **Der Weg, den das genommen hat:**

   ```
   Chat  ->  Assistent  ->  @Tool prioritaetSetzen  ->  Ticketverwaltung
         ->  Ticketstrom  ->  SSE  ->  htmx tauscht die Zeilen
   ```

   Das sind Persistenz, Nebenläufigkeit, Werkzeuge und Oberfläche in einem
   einzigen Klick.

6. **Ein Ticket anlegen lassen:** *„Leg ein Ticket an: Bei Vos Logistik KG,
   Melder Hendrik Vos, der Handscanner in der Halle lädt nicht mehr. Das ist
   dringend."* Der Assistent ruft `@Tool ticketAnlegen` und wählt Kategorie und
   Priorität selbst — bei uns wurde daraus `HARDWARE` / `HOCH`. Die Zeile
   erscheint in **beiden** Fenstern. Dasselbe geht ohne KI über das Formular
   oben auf der Seite.

7. **Die Rollentrennung.** Ein **privates** Fenster öffnen — zwei normale
   Fenster desselben Browsers teilen sich das Sitzungsplätzchen, da würde die
   zweite Anmeldung die erste ersetzen. Dort als `jonas` / `jonas` anmelden:
   dieselbe Tabelle, dieselben Aktionen, aber **kein Chatkasten**.

   Er fehlt nicht, weil er versteckt ist — er steht gar nicht erst in der Seite.
   Und wer die URL kennt und `/assistent` direkt aufruft, bekommt **403**.
   Zweimal dieselbe Regel, einmal fürs Auge und einmal für den Sachkundigen.

8. **Zum Schluss die zweite Sprache:**

   ```bash
   curl -H 'Accept-Language: en' http://localhost:8080/
   ```

   Dieselbe Seite auf Englisch. Ein Kopf in der Anfrage, eine
   `.properties`-Datei im Projekt, null Zeilen Sonderbehandlung — in der Vorlage
   steht kein einziges deutsches Wort und kein einziger Nachschlage-Aufruf, nur
   Felder eines `record`.

### Die drei erprobten Fragen

Im Chatkasten stellen; die Antworten stimmen und lassen sich nachprüfen:

| Frage | Antwort | Warum sie zieht |
|---|---|---|
| „Wie viele kritische Tickets hat die Firma Nordlicht Werften GmbH?" | **4** | Der Assistent nennt die benutzte Abfrage am Ende — **ohne JOIN**, weil es keine Stammdatentabelle gibt. Die Zahl kann man in der Tabelle nachzählen. |
| „Wie lange muss ich nach einem VPN-Abbruch warten, bevor ich mich neu verbinde, und warum?" | **sieben Minuten**, weil der Einwahlserver die alte Sitzung erst nach sechs Minuten freigibt | Steht **nur im Handbuch**, in keiner Tabelle. Das ist die Handbuchsuche, nicht die Datenbank. |
| „In welchem Fach liegt der Leihgeräteschrank?" | **Fach 14** | Ebenfalls nur im Handbuch — und so speziell, dass es niemand für Weltwissen halten kann. |

Beides beantwortet dasselbe Modell. Die erste Frage hat es sich aus der
Datenbank geholt, die zweite aus acht Markdown-Dateien neben dem Projekt. Es
entscheidet selbst, wann es was benutzt.

### Und zwei, die das Haus verlassen

| Frage | Wohin sie geht |
|---|---|
| „Wie viele T-1007 liegen noch im Lager, und wo?" | `teilNachschlagen` im **Lagersystem** auf 8082. Der Bestand entsteht dort beim Start; die Zahl steht in keiner Datei dieses Projekts. |
| „Outlook meldet 0x8004010F. Was ist das?" | `microsoft_docs_search` auf **learn.microsoft.com**. Braucht eine Internetverbindung. |

Diese Anwendung kennt beide Werkzeuge nicht. In `ki.Assistent` steht
`@McpToolBox({"lager", "wissen"})` — Namen und Beschreibungen werden beim
Verbindungsaufbau erfragt. Kommt drüben ein Werkzeug dazu, ändert sich hier
keine Zeile.

Anders als die drei Fragen darüber sind diese beiden **nicht auf eine feste
Antwort geprüft**: Die eine hängt am laufenden Lagersystem, die andere am Netz.

---

## Ohne OpenAI-Schlüssel

Der Assistent, die Triage und die Handbuchsuche brauchen einen Schlüssel und
Netz. **Alles andere nicht** — und das ist der größere Teil. Sieben
Konsolenprogramme laufen allein gegen die Datenbank:

```bash
./mvnw -pl servicedesk exec:java -Dexec.mainClass=<KLASSE>
```

| Klasse | Was zu sehen ist |
|---|---|
| `de.netzfactor.servicedesk.Fehler` | CSV-Import: **7 übernommen, 3 abgewiesen**, mit Zeilennummer und Grund je kaputter Zeile. Die guten Zeilen hinter der kaputten sind trotzdem da — mit einer Exception wären sie weg gewesen. |
| `…auswertung.Berichte` | Drei Tabellen: SLA-Quote je Kategorie, die häufigsten Melder, die Auslastung der Bearbeiter. Je rund fünfzehn Zeilen Stream-Code. |
| `…auswertung.Export` | Dieselben drei Berichte als Text, Excel und PDF. Der Schreiber kennt keinen einzigen Bericht — er liest die `@Spalte`-Annotationen. |
| `…auswertung.Feiertage` | Derselbe `Kalender` wie im SLA-Code, nur steckt hinter dem Interface diesmal ein fremder Server statt eines Lambdas. |
| `…lager.Container` | Ein selbstgebauter Mini-CDI-Container in rund fünfundzwanzig Zeilen. **Braucht das Lagersystem auf 8082.** |
| `…lager.Parallel` | Zwanzig blockierende Abfragen à 300 ms, dreimal. **Braucht das Lagersystem.** |
| `…auswertung.Eskalation` | Ein Wettlauf zweier Threads um denselben Zähler. |

Zwei Messungen daraus, die den Punkt tragen — die genauen Zahlen schwanken von
Rechner zu Rechner, die Verhältnisse nicht:

**`Eskalation`**, 200 Durchläufe über 31 offene Tickets:

| | |
|---|---:|
| erwartet | 6000 |
| ungeschützt | **5932** |
| `AtomicLong` | 6000 |

`zaehler++` ist nicht eine Anweisung, sondern drei: lesen, addieren, schreiben.
Zwei Threads lesen denselben alten Wert, und eine Erhöhung ist weg. Der
ungeschützte Zähler liegt nie zu hoch — immer zu niedrig.

**`Parallel`**, 20 Abfragen à 300 ms:

| | |
|---|---:|
| nacheinander | **6116 ms** |
| `newFixedThreadPool(4)` | **1538 ms** |
| `newVirtualThreadPerTaskExecutor` | **319 ms** |

Zwischen 1538 und 319 liegt genau eine geänderte Zeile — die mit `Executors`.
Der Code der Aufgabe ist Zeichen für Zeichen derselbe.

Und die Tests laufen ebenfalls ohne Schlüssel:

```bash
./mvnw -pl servicedesk test
# Tests run: 15, Failures: 0, Errors: 0, Skipped: 0
```

`SlaTest` startet nichts — keine Datenbank, kein Framework, nur Werte, und
läuft in Millisekunden. `TicketRessourceTest` startet die ganze Anwendung und
geht über HTTP durch. Beides sind Tests, aber nicht dieselbe Sorte, und man
merkt es an der Uhr.

---

## Wenn etwas klemmt

| Symptom | Ursache | Handgriff |
|---|---|---|
| `Keine Verbindung zur Datenbank. Laeuft 'docker compose up -d'?` | Genau das | `docker compose up -d`, dann `docker compose ps` zur Kontrolle |
| `Schema validation: missing column [gemeldetAm] in table [Ticket]` | Hibernate sucht die Spalte unter dem Java-Namen | Die Zeile `quarkus.hibernate-orm.physical-naming-strategy=…CamelCaseToUnderscoresNamingStrategy` muss in `application.properties` stehen |
| Jeder Aufruf landet auf `/anmelden`, auch `curl` | `quarkus.http.auth.permission.angemeldet.paths=/*` verlangt eine Anmeldung für alles außer Anmeldeseite, Abmelden und `htmx.min.js` | Im Browser anmelden. Für `curl` den Keks mitgeben oder die Anmeldung zum Ausprobieren kurz abschalten |
| `403` auf `/assistent`, obwohl angemeldet | Der Benutzer hat die Rolle `assistent` nicht — das ist `jonas` | Als `mara` anmelden. Genau dieser Unterschied ist Schritt 7 |
| Beide Fenster zeigen dieselbe Rolle | Zwei Fenster desselben Browsers teilen sich das Sitzungsplätzchen | Privates Fenster oder zweites Browserprofil |
| Der Assistent lehnt jede Frage ab, auch harmlose | Das Tagesbudget aus `servicedesk.assistent.tokens-pro-tag` ist aufgebraucht | Wert hochsetzen und neu starten, oder die Zeile des Benutzers in `tokenkonto` für heute löschen |
| Der Assistent kennt die Lagerwerkzeuge nicht und sagt „weiß ich nicht" | Das Lagersystem läuft nicht auf 8082 | Zweites Fenster: `./mvnw -pl lagersystem quarkus:dev` |
| `microsoft_docs_search` antwortet nicht | Der zweite MCP-Server steht im Internet | Netz prüfen. Ohne Verbindung fällt nur dieses eine Werkzeug aus, der Rest läuft weiter |
| Die Handbuchsuche findet nichts | Der Schlüssel war beim **Start** noch nicht gesetzt, oder `servicedesk.handbuch.pfad` zeigt ins Leere | `curl` auf `/assistent/handbuch`. Steht dort `0 Abschnitte`: Schlüssel exportieren und **neu starten** — aufgenommen wird nur beim Start |
| `does not have access to model text-embedding-ada-002` | Ohne Angabe nimmt LangChain4j `ada-002` | Die Zeile `…embedding-model.model-name=text-embedding-3-small` muss stehen |
| `Port already bound: 8080` | Ein alter Prozess läuft noch | Das Fenster suchen und mit Ctrl-C beenden |
| `LazyInitializationException` nach einer Schreiboperation | Die Entity hat die Transaktion verlassen | Die Abbildung auf die Ansicht gehört **in** die `@Transactional`-Methode — deshalb geben alle Methoden von `Ticketverwaltung` eine `dto.TicketAnsicht` zurück und nie ein `domain.Ticket` |
| `Log4j API could not find a logging provider` | Apache POI sucht ein Logging-Framework, das hier nicht läuft | Harmlos |

Wenn gar nichts mehr geht:

```bash
docker compose down -v && docker compose up -d
```

Danach steht die Datenbank wieder auf dem Anfangsstand — und alles, was du
selbst eingetragen hast, ist weg.

---

## Was ihr als Nächstes bauen würdet

Keine Entschuldigung, sondern eine Landkarte: Jeder Punkt hier ist im echten
Projekt eine Woche Arbeit, und an jedem lässt sich erkennen, wo man ansetzt.

**Ganz vorn: die Stammdaten.** Firma, Melder und Bearbeiter stehen hier als
Text am Ticket — dreimal derselbe Firmenname in drei Schreibweisen, und die
Auswertung zerfällt. Für diese Anwendung war das die richtige Entscheidung, weil
drei Joins nichts erklären. Im Betrieb wären es drei Tabellen mit
Fremdschlüsseln, und `Kategorie` würde vom `enum` zur Tabelle, sobald der erste
Kunde eine eigene Kategorie will. Wer das nachzieht, ändert vier Stellen: das
Schema, die Entities, den System-Prompt und die `SCHEMA`-Konstante — der Rest
merkt es nicht.

**Rechte, die diesen Namen verdienen.** Zwei feste Benutzer mit Klartext-
passwörtern sind eine Vorführung, keine Anmeldung. In Quarkus wäre der nächste
Schritt OIDC gegen einen Keycloak — und die interessante Frage ist nicht die
Technik, sondern welche Rolle das SQL-Werkzeug des Assistenten bekommt.

**Mailversand.** Ein Ticket, von dem der Melder nichts erfährt, ist ein Zettel
in einer Schublade. Der `Ticketstrom` ist dafür schon die richtige Stelle: ein
zweiter Zuhörer neben der Oberfläche, und niemand muss `Ticketverwaltung`
anfassen.

**Dateianhänge.** Der Screenshot ist bei den meisten Meldungen die halbe
Information. Das heißt Upload, ein Ablageort außerhalb der Datenbank — und die
Frage, wer die Datei später herunterladen darf, womit man wieder bei den
Rechten ist.

**Volltextsuche.** „Wo war noch mal das Ticket mit dem Kartenleser?" —
`LIKE '%kartenleser%'` geht über 60 Zeilen, über 600.000 nicht mehr. Postgres
bringt `tsvector` mit, und der pgvector-Speicher liegt ohnehin schon in
derselben Datenbank.

**Mandanten.** Sobald der zweite Kunde seinen eigenen ServiceDesk will, muss an
jede Abfrage eine Mandantenkennung — und zwar so, dass niemand sie vergessen
kann. Das ist die Entscheidung, die man am Anfang trifft oder nie.

**Ein Migrationswerkzeug.** Das Schema kommt hier aus zwei SQL-Dateien, die
genau einmal laufen, und Hibernate prüft es nur. Sobald sich das Schema im
laufenden Produkt ändert, brauchst du Flyway oder Liquibase — versionierte
Skripte, die auf jeder Umgebung in derselben Reihenfolge durchlaufen. Der Umbau
ist klein, weil `validate` schon jetzt erzwingt, dass Datenbank und Code
zusammenpassen.

---

## Zwei Dinge, die gut zu wissen sind

1. **Der Testlauf schreibt in dieselbe Datenbank.** `TicketRessourceTest` legt
   ein Ticket an und löscht es am Ende wieder. Bricht der Test vorher ab, bleibt
   ein 61. Ticket stehen.
2. **Die Kennung zählt von der höchsten vergebenen Nummer hoch.** Wird ein
   Ticket aus der Mitte gelöscht, entsteht keine Lücke; wird das letzte
   gelöscht, wird seine Nummer wiederverwendet. Für eine Schulung reicht das,
   im Betrieb nähme man eine Sequenz.
