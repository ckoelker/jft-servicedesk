# ServiceDesk

Ein kleines Ticketsystem für einen IT-Servicedesk, als Quarkus-Anwendung mit
REST, Persistenz, Tests, KI-Assistent und zweisprachiger Oberfläche. Gearbeitet
wird gegen eine echte Postgres-Datenbank in Docker, nicht gegen Attrappen.

## Loslegen in drei Schritten

```bash
# 1. Die Datenbank starten. Beim allerersten Mal legt sie Schema und
#    Beispieldaten selbst an.
docker compose up -d

# 2. Das Lagersystem starten - der ServiceDesk holt sich von dort per MCP
#    die Auskunft über Ersatzteile.
./mvnw -pl lagersystem quarkus:dev

# 3. Den ServiceDesk starten.
./mvnw -pl servicedesk quarkus:dev
```

Danach liegt die Oberfläche auf <http://localhost:8080>.

## Ports

| Dienst | Port |
|---|---|
| Postgres | 5433 |
| ServiceDesk | 8080 |
| Lagersystem | 8082 |
| MCP-Endpunkt des Lagersystems | 8082, Pfad `/mcp` |

## Anmeldung

Zwei Benutzer stehen fest in der Datenbank, angelegt beim ersten Start des
Containers:

| Benutzer | Passwort | Rollen | Sieht den Assistenten |
|---|---|---|---|
| `mara` | `mara` | `bearbeiter`, `assistent` | ja |
| `jonas` | `jonas` | `bearbeiter` | nein |

Beide sehen dieselben Tickets. An dem einen Rollenunterschied hängt, ob der
Kasten mit dem KI-Assistenten überhaupt gerendert wird — und ob `/assistent`
antwortet oder mit 403 ablehnt.

Die Passwörter stehen im Klartext in der Datenbank. Das ist für eine Vorführung
Absicht und in [datenbank/01-schema.sql](datenbank/01-schema.sql) erklärt.

## Der Assistent und seine Wächter

Vor jedem Modellaufruf laufen drei Wächter, sortiert nach dem, was sie kosten.
Der erste, der ablehnt, beendet die Kette:

| Wächter | Prüft | Kosten |
|---|---|---|
| `Tokenwaechter` | Tagesbudget des angemeldeten Benutzers | eine Datenbankabfrage |
| `Inhaltswaechter` | ob der Inhalt zulässig ist | ein Modellaufruf |
| `Themenwaechter` | ob es überhaupt um IT-Tickets geht | ein Modellaufruf |

Die beiden letzten fragen dasselbe Chatmodell, das auch den Assistenten
antreibt — kein zweiter Dienst und kein zweiter Zugang. Was jeweils als
unzulässig beziehungsweise als fachfremd gilt, steht als Text im Prompt von
`Inhaltspruefer` und `Themenpruefer` und lässt sich dort ändern, ohne eine Zeile
Code anzufassen.

Das Tagesbudget ist ein fester Wert je Benutzer aus
`servicedesk/src/main/resources/application.properties`. Gebucht wird der
tatsächliche Verbrauch, den das Modell meldet — nicht geschätzt.

## Werkzeuge über MCP

Der Assistent bekommt seine Werkzeuge aus drei Quellen: den eigenen Klassen im
Paket `ki`, dem Lagersystem und der öffentlichen Dokumentation von Microsoft.
Die letzten beiden sprechen MCP über Streamable HTTP:

| Server | Adresse | Wofür |
|---|---|---|
| `lager` | `http://localhost:8082/mcp` | Bestand, Lagerort, knappe Teile |
| `wissen` | `https://learn.microsoft.com/api/mcp` | Störungen an Windows, Office, Entra ID |

Den Endpunkt des Lagersystems kann man von Hand ansprechen — MCP ist JSON-RPC
über HTTP. Die Session-Kennung steht im Antwortkopf `Mcp-Session-Id`:

```bash
curl -s -D - -X POST http://localhost:8082/mcp \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/event-stream' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2025-06-18","capabilities":{},"clientInfo":{"name":"curl","version":"1"}}}'
```

## Was in welchem Ordner liegt

| Ordner / Datei | Inhalt |
|---|---|
| `ANLEITUNG.md` | Die Anwendung im Ganzen: die Vorführung Schritt für Schritt, zum Nachmachen zu Hause. |
| `datenbank/` | Schema und Beispieldaten als SQL. Läuft beim ersten Start des Containers einmal durch. |
| `handbuch/` | Das Servicedesk-Handbuch als Markdown. Die Wissensquelle für die Handbuchsuche. |
| `lagersystem/` | Ein eigenständiges Fremdsystem, das Ersatzteile herausgibt — per REST und per MCP. |
| `servicedesk/` | Die Anwendung. |
| `teilnehmerwuensche/` | Vorführungen zu Log4j, Profiling, RxJava, Gradle und Modernisierung. |

## Wenn etwas klemmt

```bash
# Die Datenbank auf den Anfangsstand zurücksetzen. Achtung: alles, was du
# selbst eingetragen hast, ist danach weg.
docker compose down -v && docker compose up -d

# Eine SQL-Konsole in der laufenden Datenbank öffnen.
docker exec -it servicedesk-db psql -U servicedesk -d servicedesk
```

## OpenAI-Schlüssel

Der Assistent braucht einen Schlüssel. Er gehört in keine Datei im Projekt,
sondern in die Umgebung:

```bash
export QUARKUS_LANGCHAIN4J_OPENAI_API_KEY='sk-...'
```

Dieser eine Schlüssel reicht für alles: Assistent, Handbuchsuche und beide
Wächter sprechen dasselbe Modell an.
