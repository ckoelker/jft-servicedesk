# Zusammenfassung der Woche

Eine Anwendung, neunzehn Blöcke. Der ServiceDesk ist nicht das Beispiel *zu*
einem Block, sondern der Ort, an dem alle Blöcke zusammenkommen — vom `record`
vom Montag bis zum KI-Assistenten vom Freitag.

---

## 1. Die Blöcke, je ein Beispiel im ServiceDesk

| Block | Thema | Wo man es im ServiceDesk sieht |
|---|---|---|
| **B01** | Ankommen, Erwartungsabfrage, Setup-Kontrolle | *nicht im ServiceDesk* — `beispiele/b01-*`, `Hallo.java` gegen `HalloKlassisch.java`, siehe `tag1/b01.md` |
| **B02** | Java 5 bis heute | überall; die Gegenüberstellungen selbst liegen in `beispiele/b02-java-evolution` (siehe Abschnitt 2) |
| **B03** | Überladen, Überschreiben, Verträge | [domain/Basis.java](servicedesk/src/main/java/de/netzfactor/servicedesk/domain/Basis.java) — `@MappedSuperclass` als Vererbung mit genau einem Zweck. Der `equals`/`hashCode`-Vertrag selbst: `beispiele/b03-*`, `tag1/b03.md` |
| **B04** | Generics, PECS, type erasure | [dto/Ergebnis.java](servicedesk/src/main/java/de/netzfactor/servicedesk/dto/Ergebnis.java) — `sealed interface Ergebnis<T>`. PECS und erasure werden hier nur *benutzt*, erklärt sind sie in `beispiele/b04-*` |
| **B05** | Collections und `java.time` | [auswertung/Sla.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Sla.java) — `Duration`, `LocalDate.datesUntil`, Fristrechnung ohne einen einzigen `Date` |
| **B06** | Fehler behandeln | [Meldungsimport.java](servicedesk/src/main/java/de/netzfactor/servicedesk/Meldungsimport.java) — eine kaputte CSV-Zeile ist ein **Wert**, kein Abbruch; daneben [Fehler.java](servicedesk/src/main/java/de/netzfactor/servicedesk/Fehler.java) mit `Optional` für „darf fehlen" |
| **B07** | Lambdas, method references | [auswertung/Kalender.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Kalender.java) — ein `@FunctionalInterface`, im Test ein Lambda, in Produktion der echte Feiertagsdienst. Aufgerufen als `kalender::arbeitsfrei` |
| **B08** | Stream API | [auswertung/Berichte.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Berichte.java) — drei Auswertungen, je ein paar Zeilen, `groupingBy` / `summingInt` / `counting` |
| **B09** | Annotationen und Reflection | [auswertung/Spalte.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Spalte.java) + [Berichtsschreiber.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Berichtsschreiber.java) — **eine** Annotation an den Record-Komponenten, drei Ausgabeformate (Text, Excel, PDF), und der Schreiber kennt keinen der Berichte |
| **B10** | Vom `main` zur Anwendung: Maven, Logging, CDI | [lager/Container.java](servicedesk/src/main/java/de/netzfactor/servicedesk/lager/Container.java) — Dependency Injection in 25 Zeilen, dazu [lager/Braucht.java](servicedesk/src/main/java/de/netzfactor/servicedesk/lager/Braucht.java) als selbstgebautes `@Inject`. Der Bogen: [lager/Ersatzteilpruefer.java](servicedesk/src/main/java/de/netzfactor/servicedesk/lager/Ersatzteilpruefer.java) trägt **beide** Annotationen und läuft unverändert in beiden Containern |
| **B11** | Quarkus: dev mode, Konfiguration, REST, Validation | [web/TicketRessource.java](servicedesk/src/main/java/de/netzfactor/servicedesk/web/TicketRessource.java) mit [dto/NeuesTicket.java](servicedesk/src/main/java/de/netzfactor/servicedesk/dto/NeuesTicket.java) — Bean Validation am Record, Fehler kommen als 400 zurück, ohne eine Zeile Prüfcode |
| **B12** | Persistenz: JDBC, Panache | [domain/Ticket.java](servicedesk/src/main/java/de/netzfactor/servicedesk/domain/Ticket.java) — Panache-Entity, die Abfragen als statische Methoden an der Entity selbst (`find("kennung", …)`); das nackte JDBC daneben in [domain/Datenbank.java](servicedesk/src/main/java/de/netzfactor/servicedesk/domain/Datenbank.java) |
| **B13** | Testen | [TicketRessourceTest.java](servicedesk/src/test/java/de/netzfactor/servicedesk/web/TicketRessourceTest.java) (`@QuarkusTest` + RestAssured), [SlaTest.java](servicedesk/src/test/java/de/netzfactor/servicedesk/auswertung/SlaTest.java) (reines JUnit mit Lambda-Kalender), [LagerauskunftTest.java](servicedesk/src/test/java/de/netzfactor/servicedesk/lager/LagerauskunftTest.java) (Mockito) |
| **B14** | Threads, locks, `java.util.concurrent` | [auswertung/Eskalation.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Eskalation.java) — derselbe Lauf dreimal: ungeschützter Zähler, `AtomicLong`, ohne Threads. Nur eine Zahl weicht ab, und zwar die erwartete |
| **B15** | Virtual threads, `HttpClient` | [lager/Parallel.java](servicedesk/src/main/java/de/netzfactor/servicedesk/lager/Parallel.java) — 20 Abfragen nacheinander, im Pool, mit virtuellen Threads. Der blockierende Client: [lager/Lagerauskunft.java](servicedesk/src/main/java/de/netzfactor/servicedesk/lager/Lagerauskunft.java), der echte Fremddienst: [auswertung/Feiertage.java](servicedesk/src/main/java/de/netzfactor/servicedesk/auswertung/Feiertage.java). Reaktiv gibt es an genau einer Stelle: [Ticketstrom.java](servicedesk/src/main/java/de/netzfactor/servicedesk/Ticketstrom.java), herausgereicht als SSE von `TicketRessource./tickets/strom` |
| **B16** | LangChain4j: AI services, structured output | [ki/Triagedienst.java](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Triagedienst.java) — die Schnittstelle *ist* der Dienst; [dto/Triage.java](servicedesk/src/main/java/de/netzfactor/servicedesk/dto/Triage.java) ist der Rückgabetyp, aus dem das Schema gebaut wird, und die enums liefern die erlaubten Werte |
| **B17** | Tools, function calling, RAG | [ki/Assistent.java](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Assistent.java) mit [Ticketwerkzeuge](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Ticketwerkzeuge.java) und [Datenbankwerkzeug](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Datenbankwerkzeug.java); RAG in [ki/Handbuchsucher.java](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Handbuchsucher.java) — hängt sich an jede Frage, ohne dass der Assistent davon weiß |
| **B18** | Der ServiceDesk als Ganzes | [web/Oberflaeche.java](servicedesk/src/main/java/de/netzfactor/servicedesk/web/Oberflaeche.java) + `templates/Seiten/tickets.html`; zwei Sprachen javaseitig über [web/Texte.java](servicedesk/src/main/java/de/netzfactor/servicedesk/web/Texte.java) und `meldungen_de/_en.properties` |
| **B19** | Abschluss, Lernpfade, Übergabe | dieses Repository — `tag5/b19.md` |

### Nach dem Kurs dazugekommen

Steht im Code, war aber kein eigener Block. Wer es zeigen will, hat drei Minuten
Stoff und einen sichtbaren Effekt:

| | |
|---|---|
| **Anmeldung** | `quarkus-security-jpa`: [domain/Benutzer.java](servicedesk/src/main/java/de/netzfactor/servicedesk/domain/Benutzer.java) ist eine gewöhnliche Entity mit vier Annotationen — daraus wird die Benutzerverwaltung. `mara` sieht den Assistenten, `jonas` nicht |
| **Wächter** | drei `InputGuardrail`s vor jedem Modellaufruf: Tagesbudget (Datenbankabfrage), Inhalt, Thema. Sortiert nach dem, was sie kosten |
| **MCP** | [ki/Assistent.java](servicedesk/src/main/java/de/netzfactor/servicedesk/ki/Assistent.java) holt Werkzeuge aus dem eigenen Lagersystem **und** von `learn.microsoft.com` — derselbe Dreizeiler in der Konfiguration, egal ob der Server nebenan oder im Netz läuft |

---

## 2. Java 5 bis 25 — was davon im ServiceDesk steht

| Ab | Neuerung | Beispiel |
|---|---|---|
| **5** | **Generics** | `Ergebnis<T>`, `List<Ticket>` — überall |
| 5 | Enums mit Verhalten | [domain/Prioritaet.java](servicedesk/src/main/java/de/netzfactor/servicedesk/domain/Prioritaet.java): der Faktor und die Fristrechnung stehen *im* enum, nicht in jeder Auswertung |
| 5 | Annotationen | `@Spalte` — und alles, was Quarkus liest |
| 5 | varargs | `Texte.text(schluessel, sprache, Object... werte)` |
| **7** | try-with-resources | `Meldungsimport.ausDemKlassenpfad`, `Datenbank.tickets` |
| **8** | **Lambdas / method references** | `Kalender.nurWochenende()`, `kalender::arbeitsfrei` |
| **8** | **Streams und `Collectors`** | `Berichte.slaQuote` — `groupingBy`, dann `Comparator.comparingDouble(...).reversed()` |
| 8 | `Optional` als Rückgabetyp | `Fehler.finde(...)` — der Aufrufer *muss* sich entscheiden |
| **8** | **`java.time`** | `Sla`: `Duration` und `LocalDateTime` (8), `datesUntil` (9) |
| 9–11 | `List.of`, `String.isBlank` | `Export` und `Fehler` (`List.of`), `Meldungsimport` und `Oberflaeche` (`isBlank`) |
| 9–11 | `HttpClient` | `Lagerauskunft`, `Feiertage` — der Nachfolger von `URLConnection`, blockierend benutzt |
| 12–17 | `switch`-Ausdruck | `Oberflaeche.seite`: `case "offen" -> Ticket.offene()` |
| 12–17 | Text blocks | jeder `@SystemMessage("""…""")` im Paket `ki` |
| **12–17** | **`record`** | das ganze Paket `dto` — und die Grenze dazu: `domain` sind Klassen, weil Entities eine Identität unabhängig von ihren Feldern haben |
| 12–17 | `sealed` + pattern matching | beide Zweige von `Ergebnis`: `instanceof Ergebnis.Gelungen<Meldung>` in `Meldungsimport`, `instanceof Ergebnis.Misslungen<Meldung>` in `Importergebnis` |
| **21** | **Virtual threads** | `Executors.newVirtualThreadPerTaskExecutor()` in `Parallel` — eine Zeile Unterschied, ein anderer Messwert |
| 19/21 | `ExecutorService` ist `AutoCloseable` | `try (dienst)` in `Parallel` — dasselbe `try` wie 2011, nur dass ein Executor jetzt eine Ressource ist |
| 25 | flexible constructor bodies, compact source files | *nicht im ServiceDesk* — `beispiele/b02-java-evolution`, Paket `v25`, und `Hallo.java` aus B01 |

Der Satz, der den Java-Teil zusammenhält: **die neuen Sprachmittel ändern nicht,
was der Code tut — sie ändern, wie viel man lesen muss, um es zu erkennen.**

---

## 3. Werkzeuge und Dienste

| | Wofür | Wo |
|---|---|---|
| **JDK 25** | die Sprache | `maven.compiler.release=25` |
| **Maven Wrapper** | ein Bau, überall gleich | `./mvnw` — kein lokales Maven nötig |
| **Quarkus 3.38.3** | dev mode, CDI, REST, Konfiguration | `./mvnw quarkus:dev`, Änderung speichern, Seite neu laden |
| **Hibernate ORM + Panache** | Persistenz | `domain/` — die Datenbank ist die Wahrheit, Hibernate prüft das Schema nur (`validate`) |
| **PostgreSQL 17 in Docker** | die echte Datenbank, keine Attrappe | `pgvector/pgvector:pg17`, `docker compose up -d`, Port 5433 |
| **Qute + htmx** | die Oberfläche | `templates/Seiten/` — Server rendert, htmx tauscht Fragmente |
| **JUnit 5, AssertJ, Mockito, RestAssured** | Tests | `src/test/` — `./mvnw verify` |
| **Apache POI, OpenPDF** | Excel und PDF aus demselben Record | `Berichtsschreiber` |
| **LangChain4j (Quarkus-Erweiterung)** | AI services, Tools, Guardrails | `ki/` |
| **OpenAI** | Chatmodell und Einbettungen | ein Schlüssel für alles, nur als Umgebungsvariable |
| **pgvector** | der Speicher für die Handbuchsuche | dieselbe Postgres-Instanz, eigene Tabelle |
| **MCP (Streamable HTTP)** | fremde Werkzeuge einbinden | `lagersystem` auf `:8082/mcp`, `learn.microsoft.com/api/mcp` |
| **date.nager.at** | deutsche Feiertage | `Feiertage` — der einzige Fremddienst ohne Schlüssel |

---

## 4. Wo was liegt

- **`servicedesk/`** — die Anwendung
- **`lagersystem/`** — das Fremdsystem: dieselben Daten per REST *und* per MCP
- **`handbuch/`** — die Wissensquelle für RAG, schlichtes Markdown
- **`datenbank/`** — Schema und Beispieldaten als SQL
- **`teilnehmerwuensche/`** — Log4j, Profiling, RxJava, Gradle, Modernisierung

Der Rest — Blockhandbücher, Übungen, Musterlösungen, Folien — liegt im Kurs-Repo
und im Dozenten-Repo. Alles, was hier oben mit *nicht im ServiceDesk* markiert
ist, findet sich dort unter der genannten Stelle.
