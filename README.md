# ServiceDesk

Das hier ist die Anwendung, an der du ab Dienstagnachmittag arbeitest: ein
kleines Ticketsystem für einen IT-Servicedesk. Sie fängt als eine einzige
Klasse mit einer `main`-Methode an und wächst dann Block für Block mit — bis
sie am Freitag eine Quarkus-Anwendung mit REST, Persistenz, Tests, KI und
Oberfläche ist. Gearbeitet wird von Anfang an gegen eine echte
Postgres-Datenbank in Docker, nicht gegen Attrappen.

## Loslegen in drei Schritten

```bash
# 1. Die Datenbank starten. Beim allerersten Mal legt sie Schema und
#    Beispieldaten selbst an.
docker compose up -d

# 2. Den ServiceDesk laufen lassen.
./mvnw -pl servicedesk exec:java -Dexec.mainClass=de.netzfactor.servicedesk.Start

# 3. Nur wenn ein Block es verlangt: das Lagersystem nebenher starten.
./mvnw -pl lagersystem quarkus:dev
```

## Ports

| Dienst | Port |
|---|---|
| Postgres | 5433 |
| ServiceDesk | 8080 |
| Lagersystem | 8082 |

## Was in welchem Ordner liegt

| Ordner / Datei | Inhalt |
|---|---|
| `datenbank/` | Schema und Beispieldaten als SQL. Läuft beim ersten Start des Containers einmal durch. |
| `handbuch/` | Das Servicedesk-Handbuch als Markdown. Ab Freitag die Wissensquelle für die Suche. |
| `lagersystem/` | Ein fertiges Fremdsystem, das Ersatzteile herausgibt. Wird nicht verändert. |
| `servicedesk/` | Dein Projekt. Hier entsteht alles. |
| `BAUSTEINE.md` | Die längeren Abschnitte zum Kopieren, damit du im Kurs nicht tippst. |

## Die `pom.xml` wächst mit

In `servicedesk/pom.xml` steht schon alles drin, was die Woche über gebraucht
wird — das meiste davon auskommentiert, mit einer Blockmarke wie
`B12: Persistenz` davor. Jeder Block der Schulung nimmt bei seinem Abschnitt
nur die Kommentarzeichen weg; abgetippt wird nichts. Ein Blick in die Datei
zeigt dir deshalb jederzeit, wie weit die Anwendung ist. Die
`application.properties` ist genauso aufgebaut.

## Was schon fertig im Projekt liegt

Ein Teil ist Beiwerk und kein Stoff — den findest du beim Klonen bereits vor,
damit die Zeit in die Sache geht statt ins Tippen:

| Paket | Was |
|---|---|
| `domain` | `Ticket`, `Kommentar`, `Zeitbuchung` — noch als gewöhnliche Klassen ohne eine einzige Annotation. In Block 12 kommen die dazu, die Felder bleiben. |
| `domain` | `Prioritaet`, `Status`, `Kategorie` — die drei Aufzählungen. `Kategorie` bringt die zugesagten Stunden gleich mit. |
| `dto` | `TicketAnsicht`, `KommentarAnsicht`, `Ereignis`, `Meldung`, `Ergebnis`, `Triage`, `Eskalationslauf` — records, die nur Werte weiterreichen. |

Alles andere entsteht im Kurs: getippt, was den Punkt des Blocks ausmacht,
kopiert aus `BAUSTEINE.md`, was nur Fleißarbeit wäre.

## Wenn etwas klemmt

```bash
# Die Datenbank auf den Anfangsstand zurücksetzen. Achtung: alles, was du
# selbst eingetragen hast, ist danach weg.
docker compose down -v && docker compose up -d

# Eine SQL-Konsole in der laufenden Datenbank öffnen.
docker exec -it servicedesk-db psql -U servicedesk -d servicedesk
```

## OpenAI-Schlüssel

Ab Block 16 brauchst du einen Schlüssel. Er gehört in keine Datei im
Projekt, sondern in die Umgebung:

```bash
export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY='sk-...'
```
