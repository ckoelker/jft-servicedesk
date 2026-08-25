# Log4j mit Rotation

Die Lagerverwaltung protokolliert mit der Log4j-2-API in eine Datei, die sich
selbst dreht.

```bash
./mvnw -pl lagersystem quarkus:dev
curl localhost:8082/teile
ls lagersystem/logs/
```

## Warum überhaupt ein Logger

`System.out` kann man nicht abschalten, nicht einstufen und nicht umleiten.
Ein Logger kann alles drei — und das Wichtigste: Die Datei wächst nicht ins
Unendliche, weil sie **rotiert**.

Im Code sieht das so aus:

```java
private static final Logger LOG = LogManager.getLogger(Lager.class);

LOG.debug("Teil {} gefunden: {}", nummer, teil.bezeichnung());
```

Die `{}` sind kein Schönheitsdetail. Ist DEBUG abgeschaltet, wird die
Zeichenkette gar nicht erst zusammengebaut — bei `"..." + wert` schon.

## Der Stolperstein: Log4j unter Quarkus

Ein Projekt mit Log4j nimmt normalerweise `log4j-api` und `log4j-core` und
beschreibt die Rotation in einer `log4j2.xml` mit einem `RollingFileAppender`.

**Unter Quarkus geht das nicht.** Quarkus bringt seinen eigenen LogManager mit,
und zwei Umsetzungen im selben Lauf gehen nicht gut aus. Deshalb steht in der
`pom.xml` nur eine Abhängigkeit:

```xml
<dependency>
    <groupId>org.jboss.logmanager</groupId>
    <artifactId>log4j2-jboss-logmanager</artifactId>
</dependency>
```

Der Adapter bringt die Log4j-API mit und leitet sie an Quarkus weiter. Folge:

- **Der Code bleibt Log4j.** `LogManager.getLogger(...)`, `LOG.debug("{}", x)` —
  nichts daran ändert sich.
- **Eine `log4j2.xml` liest niemand.** Eingestellt wird in der
  `application.properties`.
- **Das Musterformat ist ein anderes.** `%s` ist die Nachricht, nicht `%m`.
  Der Adapter übersetzt die API, nicht das Format.

Das ist der Punkt, den man kennen muss: „Wir nehmen Log4j" heißt unter Quarkus
*die Log4j-API*, nicht *die Log4j-Konfiguration*.

## Die Rotation

```properties
quarkus.log.file.enabled=true
quarkus.log.file.path=logs/lagersystem.log
quarkus.log.file.level=DEBUG

quarkus.log.file.rotation.max-file-size=256K
quarkus.log.file.rotation.max-backup-index=5
quarkus.log.file.rotation.file-suffix=.yyyy-MM-dd
quarkus.log.file.rotation.rotate-on-boot=true
```

- **`max-file-size`** — 256 KB ist absichtlich klein, damit man die Rotation im
  Kurs auch wirklich passieren sieht. Im Betrieb wären 10M üblich.
- **`max-backup-index`** — fünf Altstände, dann fällt der älteste weg. **Ohne
  diese Zeile räumt niemand auf**; der häufigste Rotationsfehler ist eine
  Platte, die trotzdem volläuft.
- **`file-suffix`** mit Datum schaltet zusätzlich die tägliche Rotation ein.
  Gedreht wird dann bei 256 KB *und* beim Tageswechsel.

Ergebnis nach ein paar Läufen:

```
lagersystem.log
lagersystem.log.2026-08-25.2
lagersystem.log.2026-08-25.3
```

## Zwei Ziele, ein Logger

```properties
quarkus.log.console.level=INFO
quarkus.log.file.level=DEBUG
quarkus.log.category."de.netzfactor".level=DEBUG
```

Die Konsole zeigt den Ablauf, die Datei zeigt die Einzelheiten. Kein
`if (debugEnabled)` im Code nötig.

## Achtung beim Abschreiben

`quarkus.log.file.enable` (ohne `d`) funktioniert noch, ist aber **deprecated**
und meldet sich beim Start mit einer Warnung. Richtig ist
`quarkus.log.file.enabled`.
