# Teilnehmerwünsche

Themen, die während der Woche aus der Gruppe kamen und nicht im Plan standen.

**Das hier ist kein Pflichtstoff.** Nichts davon musst du selbst bauen — es
wird vorgeführt, und der Code liegt fertig da, damit du später nachlesen kannst.

| Datei | Thema |
|---|---|
| [LOG4J.md](LOG4J.md) | Log4j 2 mit Rotation — und warum es unter Quarkus anders aussieht |
| [PROFILING.md](PROFILING.md) | Vier eingebaute Fehler und wie man sie grafisch findet |
| [RXJAVA.md](RXJAVA.md) | Was von RxJava bleibt, seit es virtuelle Threads gibt |
| [MODERNISIEREN.md](MODERNISIEREN.md) | Ein altes Java-5-Projekt auf JDK 25 ziehen |

## Wo der Code liegt

Alles im Modul `lagersystem/` — der Lagerverwaltung, die als Fremdsystem
nebenher läuft. **Nicht** im `servicedesk/`: Das ist dein Projekt und bleibt
schlank, damit die Blockfolge der Woche nicht durcheinandergerät.

```bash
./mvnw -pl lagersystem quarkus:dev
curl localhost:8082/showcase       # die Profiling-Fälle
curl localhost:8082/ereignisse     # die RxJava-Vorführung
ls lagersystem/logs/               # die rotierten Logdateien
```

Kaputtmachen kostet hier nichts. Ein Neustart räumt auf.
