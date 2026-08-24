# Arbeitsweise im ServiceDesk

Der Weg eines Tickets von der Annahme bis zum Abschluss — gültig für alle fünf
Kunden und alle sechs Kategorien.

## Servicezeit und Erreichbarkeit

- Servicezeit ist Montag bis Freitag von 07:30 bis 17:30 Uhr.
- Telefon: 04941 / 8 08 08-0. Sammelruf auf alle fünf Arbeitsplätze.
- E-Mail: servicedesk@servicedesk.de. Jede Mail erzeugt automatisch ein Ticket.
- Selbstbedienungsportal: rund um die Uhr, Tickets landen im Status NEU.
  Ausserhalb der Servicezeit greift die Rufbereitschaft (`03-eskalation.md`).

## Annahme

Jede Meldung wird sofort als Ticket erfasst — auch dann, wenn sie im Gespräch
gelöst wird. Ein Ticket ohne Erfassung gilt als nicht bearbeitet.
Pflichtangaben bei der Annahme:

1. Melder mit Namen und Firma
2. Betroffenes Gerät oder System
3. Was genau passiert, im Wortlaut des Melders
4. Seit wann das Problem auftritt
5. Rückrufnummer, wenn die Nummer im Verzeichnis nicht stimmt

Die Ticketkennung wird automatisch vergeben und hat die Form `S-0042` — Präfix
`S-`, danach vier Stellen laufende Nummer. Sie wird dem Melder gleich genannt.

## Einordnung

Der Dispatch des Tages ordnet jedes neue Ticket ein — Kategorie, Priorität,
Bearbeiter. Die Rolle wechselt täglich in der Reihenfolge mkr, jfe, sah, tre,
nba und beginnt um 07:30. Neue Tickets sind bis 09:00 Uhr vollständig verteilt,
danach laufend innerhalb von 30 Minuten nach Eingang.

### Fachpaten je Kategorie

| Kategorie | Fachpate | Vertretung |
|---|---|---|
| ZUGANG | Sina Ahrens (sah) | Mara Kruse (mkr) |
| NETZ | Jonas Feld (jfe) | Nils Baumann (nba) |
| DRUCKER | Tobias Renk (tre) | Sina Ahrens (sah) |
| HARDWARE | Tobias Renk (tre) | Jonas Feld (jfe) |
| SOFTWARE | Nils Baumann (nba) | Mara Kruse (mkr) |
| SONSTIGES | Mara Kruse (mkr) | Nils Baumann (nba) |

### Kundenpaten

| Kunde | Kundenpate |
|---|---|
| Nordlicht Werften GmbH | Jonas Feld (jfe) |
| Stadtwerke Aurich | Nils Baumann (nba) |
| Kontor Sued AG | Sina Ahrens (sah) |
| Praxis Dr. Hansen | Tobias Renk (tre) |
| Vos Logistik KG | Mara Kruse (mkr) |

Fachpate schlägt Kundenpate. Der Kundenpate wird nur zuständig, wenn Fachpate
und Vertretung beide ausfallen oder wenn das Ticket eine vereinbarte
Sonderregel des Kunden berührt (siehe `04-kunden.md`).

### Prioritäten

- **KRITISCH** — der Betrieb steht: mehr als fünf Personen betroffen oder ein
  Produktions-, Kassen- oder Dispositionssystem ausgefallen.
- **HOCH** — eine Person kann nicht arbeiten, es gibt keinen Ausweichweg.
- **NORMAL** — Standardfall, Arbeit eingeschränkt, aber möglich.
- **NIEDRIG** — Wunsch, Nachfrage, Vorbereitung ohne Termindruck.

Die Priorität wird nur von der Teamleitung gesenkt, nie vom Bearbeiter selbst.
Erhöhen darf jeder — mit einer Begründung im Ticket.

## Bearbeitung

- Status NEU heisst: noch niemand hat damit begonnen.
- Status IN_ARBEIT setzt der Bearbeiter, sobald er den ersten Handgriff tut.
- Status WARTET wird nur gesetzt, wenn wir auf jemanden warten, der nicht bei
  uns sitzt — Kunde, Hersteller, Lieferung. Er stoppt die Zusagezeit.
- Status ERLEDIGT setzt nur, wer die Wirkung geprüft hat.

Jeder Arbeitsschritt bekommt eine Zeitbuchung in Minuten, gerundet auf volle
15 Minuten, spätestens am selben Tag bis 17:30.

Ein Ticket wird abgegeben, wenn nach 60 Minuten Arbeit kein Fortschritt
erkennbar ist — an die Vertretung des Fachpaten, nicht an einen beliebigen
Kollegen. Vermerkt wird: was versucht wurde, was ausgeschlossen ist, was als
Nächstes zu prüfen wäre.

## Rückrufe

Ein Rückruf ist fällig, wenn das Ticket das Rückrufkennzeichen `R` trägt, wenn
die Priorität HOCH oder KRITISCH ist, wenn wir das Ticket auf WARTET setzen und
etwas vom Melder brauchen, wenn die Lösung eine Handlung am Arbeitsplatz des
Melders verlangt oder wenn wir die Priorität gegenüber der Meldung gesenkt
haben.

Fristen für den ersten Kontakt, gerechnet in Servicezeit:

| Priorität | Erstkontakt spätestens nach |
|---|---|
| KRITISCH | 15 Minuten |
| HOCH | 30 Minuten |
| NORMAL | 2 Stunden |
| NIEDRIG | 8 Stunden |

Erreichen wir den Melder nicht, wird das im Ticket notiert und nach zwei Stunden
erneut versucht. Nach drei erfolglosen Versuchen geht eine Mail raus und das
Ticket wandert auf Wiedervorlage.

## Wiedervorlage und Abschluss

- Ein Ticket auf Wiedervorlage wird alle zwei Werktage nachgefasst, höchstens
  dreimal. Bleibt die Antwort aus, wird mit dem Vermerk „ohne Rückmeldung des
  Melders" geschlossen und der Melder darüber per Mail informiert.
- WARTET darf höchstens zehn Werktage am Stück stehen; danach entscheidet die
  Teamleitung, ob geschlossen oder eskaliert wird.
- Jeder Abschluss braucht eine Abschlussnotiz mit drei Angaben: Ursache,
  Massnahme, Prüfung. Ein Satz je Angabe genügt, aber alle drei müssen da sein;
  sonst kommt das Ticket freitags in der Wochenprüfung zurück.
- Ein geschlossenes Ticket wird nie wieder geöffnet. Meldet sich der Melder
  erneut, entsteht ein neues Ticket mit Verweis auf die alte Kennung.
