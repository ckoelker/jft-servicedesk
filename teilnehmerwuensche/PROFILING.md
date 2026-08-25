# Profiling: vier Fehler und wie man sie sieht

Ein Profiler sagt dir nicht „langsam". Er sagt dir **wo**. Damit man das übt,
hat die Lagerverwaltung vier eingebaute Fehler — jeder mit einem anderen Bild.

```bash
./mvnw -pl lagersystem quarkus:dev
curl localhost:8082/showcase
```

Der ServiceDesk bleibt davon unberührt. Die Lagerverwaltung ist ein
Fremdsystem, das nebenher läuft — kaputtmachen kostet nichts, ein Neustart
räumt auf.

## Die vier Fälle

Jeder gibt eine Zeile zurück: Dauer, Ergebnis, Heap. Drei davon gibt es in
zwei Fassungen — `?schnell=true` rechnet **dasselbe Ergebnis** ohne den Fehler.
Der Vergleich der beiden Profile ist der eigentliche Lerneffekt.

| Endpunkt | Der Fehler | Gemessen | Was du im Bild siehst |
|---|---|---|---|
| `/showcase/suche` | `List.contains` in einer Schleife | **684 ms → 1 ms** | ein Kern auf 100 %, **eine** Methode als breiter Balken |
| `/showcase/blockade` | alle Threads durch ein `synchronized` | **9676 ms → 303 ms** | Dauer hoch, CPU niedrig, Threads auf `BLOCKED` |
| `/showcase/allokation` | `String +=` in der Schleife | 213 ms, Heap +190 MB | Heap sägt, GC-Zeit hoch, keine eigene Methode fällt auf |
| `/showcase/leck` | Cache ohne Ausgang | 100 MB, bleibt liegen | **die untere Kante der Sägezahnkurve steigt mit** |

Die ersten beiden sind das Paar, das man verwechselt: Beim Hotspot **arbeitet**
die CPU, bei der Blockade **wartet** sie. Beides fühlt sich als „langsam" an.

```bash
curl "localhost:8082/showcase/suche?anzahl=20000"
curl "localhost:8082/showcase/suche?anzahl=20000&schnell=true"

curl "localhost:8082/showcase/blockade?threads=32"
curl "localhost:8082/showcase/blockade?threads=32&schnell=true"

curl "localhost:8082/showcase/leck?bloecke=100"
curl -X DELETE localhost:8082/showcase/leck      # die Gegenprobe
```

Die 9676 ms sind keine Willkür: 32 Abfragen à 300 ms, eine nach der anderen.
Ohne das Schloss läuft dieselbe Arbeit nebenläufig — 303 ms, die Dauer einer
einzigen Abfrage.

## Womit man hinschaut

| Werkzeug | Woher | Wofür |
|---|---|---|
| **JConsole** | liegt im JDK, `jconsole` | sofort da. Heap-Diagramm, Threads. Reicht für `leck` und `blockade` |
| **VisualVM** | [visualvm.github.io](https://visualvm.github.io) | der Arbeitsesel: Sampler für `suche`, Heap-Dump für `leck` |
| **JFR + JDK Mission Control** | JFR im JDK, [JMC bei Adoptium](https://adoptium.net/jmc/) | der Profi-Weg, läuft auch im Betrieb mit |
| `jcmd` | liegt im JDK | ohne Oberfläche: Thread-Dump, Heap-Dump, JFR starten |

Ein Dienst hat einen Vorteil gegenüber einem Programm mit `main`: Er **läuft
weiter**. Man kann den Profiler in Ruhe anhängen und dann die Anfrage schicken.

## Rezept 1 — VisualVM, für `suche`

1. `quarkus:dev` läuft, VisualVM öffnen, den Prozess doppelklicken.
2. Reiter **Sampler** → **CPU** → *Sample*.
3. Jetzt `curl "localhost:8082/showcase/suche?anzahl=20000"` — am besten
   mehrfach hintereinander.
4. `ArrayList.contains` steht oben. Das ist der Hotspot.
5. Dasselbe mit `&schnell=true`. Der Balken ist weg.

Für `allokation` denselben Weg über **Sampler → Memory**: Dort dominiert nicht
dein Code, sondern das Kopieren von `byte[]`/`char[]`. Genau das ist die Aussage.

## Rezept 2 — Heap-Dump, für `leck`

```bash
curl "localhost:8082/showcase/leck?bloecke=100"
jcmd <PID> GC.heap_dump C:/tmp/leck.hprof
```

In VisualVM öffnen → **Klassen** nach Größe sortieren → `byte[]` steht oben →
Rechtsklick **In Instanzen anzeigen** → bei einer Instanz **Verweise**
aufklappen. Der Pfad endet bei `Fehlerspeicher.abgelegt`.

Damit ist nicht nur belegt, *dass* etwas leckt, sondern **wer es festhält** —
und das ist die Frage, auf die es ankommt.

Danach `curl -X DELETE localhost:8082/showcase/leck` und im Heap-Diagramm
zusehen, wie es zurückfällt. Erst diese Gegenprobe macht aus einer Vermutung
einen Befund.

## Rezept 3 — Threads, für `blockade`

```bash
curl "localhost:8082/showcase/blockade?threads=32" &
jcmd <PID> Thread.print
```

Oder in JConsole der Reiter **Threads**. Viele Threads auf `BLOCKED`, alle am
selben Monitor — daneben eine CPU-Auslastung nahe null.

## Rezept 4 — JFR, wenn es im Betrieb passiert

```bash
jcmd <PID> JFR.start name=lauf duration=60s filename=C:/tmp/lauf.jfr
```

Die Datei in JDK Mission Control öffnen. JFR ist der einzige der vier Wege, den
man auf einem Produktivsystem mitlaufen lassen kann — der Overhead liegt bei
etwa einem Prozent.

## Abschalten

Die Endpunkte sind im Betrieb Sprengstoff. Sie hängen an einem Schalter:

```properties
lagersystem.showcase.aktiv=false
```

## Merksatz

Miss, bevor du änderst. Alle vier Fehler sehen im Quelltext harmlos aus;
gefunden hat sie keiner durch Hinschauen.
