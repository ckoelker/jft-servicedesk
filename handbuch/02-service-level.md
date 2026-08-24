# Service Level

Die Zusage (SLA) sagt, wie lange es höchstens dauern darf, bis ein Ticket
erledigt ist. Sie ist keine Reaktionszeit — die Reaktionszeit steht als
Erstkontaktfrist in `01-arbeitsweise.md`.

## Grundzusage je Kategorie

| Kategorie | Bezeichnung | Grundzusage |
|---|---|---|
| ZUGANG | Zugang und Passwort | 4 Stunden |
| NETZ | Netzwerk und VPN | 8 Stunden |
| DRUCKER | Drucker und Scanner | 24 Stunden |
| HARDWARE | Hardware und Geräte | 24 Stunden |
| SOFTWARE | Software und Updates | 48 Stunden |
| SONSTIGES | Sonstiges | 72 Stunden |

## Wie die Priorität die Zusage verändert

Die Grundzusage wird mit einem Faktor multipliziert:

| Priorität | Faktor | Wirkung |
|---|---|---|
| NIEDRIG | 2 | verdoppelt die Zusage |
| NORMAL | 1 | lässt die Zusage unverändert |
| HOCH | 0,5 | halbiert die Zusage |
| KRITISCH | 0,25 | viertelt die Zusage |

Daraus ergibt sich diese Tabelle. Sie ist verbindlich; es wird nichts
zusätzlich gerundet.

| Kategorie | NIEDRIG | NORMAL | HOCH | KRITISCH |
|---|---|---|---|---|
| ZUGANG | 8 h | 4 h | 2 h | 1 h |
| NETZ | 16 h | 8 h | 4 h | 2 h |
| DRUCKER | 48 h | 24 h | 12 h | 6 h |
| HARDWARE | 48 h | 24 h | 12 h | 6 h |
| SOFTWARE | 96 h | 48 h | 24 h | 12 h |
| SONSTIGES | 144 h | 72 h | 36 h | 18 h |

## Wie die Uhr läuft

- Die Uhr startet mit dem Zeitpunkt der Meldung, nicht mit der Einordnung.
- Gezählt wird **Servicezeit**: Montag bis Freitag 07:30 bis 17:30, also zehn
  Stunden je Werktag.
- Wochenenden zählen nicht. Ein Ticket, das freitags um 16:30 mit acht Stunden
  Zusage eingeht, ist montags um 15:30 fällig.
- Gesetzliche Feiertage in Niedersachsen zählen nicht. Massgeblich ist der
  Standort des Kunden; für Kontor Sued AG gelten die Feiertage in Hamburg.
- Der 24. und der 31. Dezember gelten in voller Länge als arbeitsfreie Tage.
- **Ausnahme KRITISCH:** Bei Priorität KRITISCH läuft die Uhr rund um die Uhr
  weiter, auch nachts, am Wochenende und an Feiertagen.
- Der Status WARTET stoppt die Uhr. Sie läuft weiter, sobald der Status
  zurückgesetzt wird. Alle anderen Status lassen die Uhr laufen.
- Bricht der Melder ein Ticket ab, gilt die Zusage als eingehalten.

## Messung

- Die Erstlösungsquote ist der Anteil der Tickets, die ohne Abgabe an einen
  zweiten Bearbeiter geschlossen werden. Zielwert: 62 Prozent im Monat.
- Die Einhaltungsquote ist der Anteil der Tickets, die innerhalb der Zusage
  erledigt wurden. Zielwert: 95 Prozent im Monat, je Kunde gerechnet.
- Ausgewertet wird am dritten Werktag des Folgemonats. Die Auswertung geht an
  die Teamleitung und an die Ansprechpartner der Kunden.

## Eskalationsstufen bei drohender Überschreitung

Die Stufen hängen daran, wie viel der Zusagezeit verbraucht ist. Gemeint ist
immer die nach Priorität berechnete Zusage aus der Tabelle oben.

| Stufe | Auslöser | Wer wird informiert | Wie |
|---|---|---|---|
| 1 | 50 Prozent verbraucht, Ticket nicht in Arbeit oder ohne Fortschritt | Fachpate und Teamleitung | Hinweis im System, mündlich im Team |
| 2 | 80 Prozent verbraucht | Teamleitung, Ansprechpartner des Kunden | Eskalationsmail, bei HOCH und KRITISCH zusätzlich Anruf |
| 3 | Zusage überschritten | Betriebsleitung, Ansprechpartner des Kunden | Anruf innerhalb von 30 Minuten, danach Eskalationsmail |

Ergänzend:

- Bei KRITISCH wird Stufe 2 immer sofort ausgelöst, unabhängig vom
  Verbrauchsstand — die Teamleitung erfährt von jedem kritischen Ticket
  innerhalb von 15 Minuten.
- Ein Ticket, das Stufe 2 erreicht hat, darf nicht mehr auf WARTET gesetzt
  werden, ohne dass die Teamleitung zustimmt.
- Nach Stufe 3 bekommt das Ticket einen festen Bearbeiter zugewiesen, der bis
  zum Abschluss keine neuen Tickets annimmt.

## Was nach einer Überschreitung passiert

1. Der Bearbeiter schreibt binnen eines Werktags eine Ursachennotiz ins
   Ticket: warum die Zusage nicht gehalten wurde.
2. Die Teamleitung sichtet alle Überschreitungen freitags von 14:00 bis 15:00.
3. Überschreitungen mit gleicher Ursache in zwei aufeinanderfolgenden Wochen
   werden als Serienstörung geführt und bekommen ein eigenes Sammelticket.
4. Der Kunde bekommt zu jeder Überschreitung eine kurze schriftliche
   Rückmeldung, spätestens mit dem Monatsbericht.

Zusagen werden nicht nachträglich angepasst. Wird eine Priorität im Lauf der
Bearbeitung geändert, gilt die neue Zusage ab dem Zeitpunkt der Änderung; die
bereits verbrauchte Zeit bleibt angerechnet.
