# RxJava — was davon bleibt

Die Lagerverwaltung hat eine Zusatzfunktion, die es nur für dieses Thema gibt.
Der ServiceDesk ruft sie nie auf.

```bash
./mvnw -pl lagersystem quarkus:dev

curl localhost:8082/ereignisse                 # mit RxJava
curl "localhost:8082/ereignisse?fassung=hand"  # dasselbe von Hand
```

## Der Fall

An einem Ersatzteil wird dreimal kurz hintereinander gebucht. Daraus soll
**eine** Nachbestellwarnung werden, nicht drei. Sechs Buchungen gehen hinein,
vier Warnungen kommen heraus.

Mit RxJava sind das zwei Zeilen:

```java
.groupBy(Buchung::teil)                                  // ein Strom je Teil
.flatMap(jeTeil -> jeTeil.debounce(300, MILLISECONDS))   // Ruhe abwarten
```

`?fassung=hand` rechnet dasselbe ohne die beiden Operatoren: zwei Maps, ein
Zeitstempel je Teil, eine Entscheidung bei jeder Buchung und am Ende
Nachräumen. Der Zustand steht jetzt im eigenen Code, und mit ihm vier Stellen,
an denen man sich irren kann.

Und eine davon ist gleich sichtbar. Vergleich die Zeitstempel der beiden
Aufrufe:

```
[rx]    589 ms · 1077 ms · 1578 ms · 1779 ms
[hand] 1166 ms · 1667 ms · 1667 ms · 1667 ms
```

Beide zählen vier Warnungen — aber die Handfassung liefert sie **gebündelt am
Ende**, weil ihr der Timer fehlt, der nach 300 ms Ruhe von selbst auslöst. Sie
merkt erst bei der *nächsten* Buchung, dass Ruhe war. Für eine Nachbestellung
ist das der Unterschied zwischen einer Warnung und einem Protokolleintrag.

Wer das nachrüstet, braucht je Teil einen geplanten Timer, muss ihn bei jeder
neuen Buchung abbestellen und am Ende alles aufräumen. Das sind die zwei Zeilen
oben.

**Das ist der Grund, aus dem RxJava bleibt: Rechnen über die Zeit.**
`debounce`, `throttle`, `window`, `buffer`, `timeout` — dafür hat die
Standardbibliothek bis heute nichts Vergleichbares.

## Warum virtuelle Threads es sonst abgelöst haben

Reaktive Bibliotheken sind in die meisten Projekte aus **einem** Grund
eingezogen: Ein Thread je Anfrage war zu teuer, also durfte nichts mehr
blockieren. Wer nicht blockieren darf, muss seinen Ablauf in Rückrufe zerlegen
— und `flatMap` war der Preis dafür, nicht das Ziel.

Dieser Grund ist weg. Ein virtueller Thread kostet fast nichts, also darf man
wieder blockieren.

| | RxJava | Virtuelle Threads |
|---|---|---|
| Drei Systeme parallel abfragen | `flatMap` + `subscribeOn` | `invokeAll`, gewöhnlicher Code |
| Dauer | gleich | gleich |
| Stacktrace bei einem Fehler | im Operator | in der eigenen Methode |
| Debugger, Breakpoints | schwierig | funktionieren |
| Reihenfolge des Ergebnisses | nach Antwortzeit | nach Auftrag |
| Entprellen, Zeitfenster | **kann es** | **kann es nicht** |

Die Zeile, auf die es ankommt, ist die letzte.

## Merksatz

Nebenläufigkeit können virtuelle Threads besser — lesbarer, und im Debugger
bleibt man in der eigenen Methode. Rechnen über die Zeit können sie gar nicht.

Wer heute reaktiv anfängt, sollte sagen können, welchen Zeitoperator er
braucht. Fällt ihm keiner ein, ist es der falsche Weg.
