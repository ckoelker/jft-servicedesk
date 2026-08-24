# Eskalation, Rufbereitschaft, Wochenenddienst

Eskalation ist kein Vorwurf, sondern ein Verfahren. Wer eskaliert, hat richtig
gehandelt; wer eine Überschreitung verschweigt, nicht.

## Die Kette

| Stufe | Rolle | Person | Erreichbar |
|---|---|---|---|
| 1 | Teamleitung ServiceDesk | Mara Kruse (mkr) | Servicezeit, mobil 0170 / 4 12 90 31 |
| 2 | Betriebsleitung | Kirsten Vogt | Mo–Fr 08:00–18:00, mobil 0170 / 4 12 90 12 |
| 3 | Geschäftsführung | Henning Brammer | Mo–Fr 08:00–18:00, mobil 0170 / 4 12 90 01 |

Ist Mara Kruse abwesend, übernimmt Nils Baumann (nba) die Stufe 1. Ist Kirsten
Vogt abwesend, springt die Stufe 1 direkt auf Henning Brammer.

Eine Stufe wird nie übersprungen — mit einer Ausnahme: Bei einem Datenabfluss,
einem Verdacht auf Schadsoftware im Kundennetz oder einer Meldung mit
Personenbezug wird sofort Stufe 3 informiert, parallel zu Stufe 1.

## Auslöser

Eskaliert wird bei:

- Erreichen der Schwellen 50 / 80 / 100 Prozent der Zusagezeit (siehe
  `02-service-level.md`)
- jedem Ticket mit Priorität KRITISCH, sofort und ohne Schwelle
- einer Serienstörung mit mehr als drei betroffenen Meldern
- einem Ausfall, der mehrere Kunden gleichzeitig betrifft
- jeder Forderung des Kunden nach einem Gespräch oberhalb des Bearbeiters

## Wann der Kunde aktiv angerufen wird

Nicht jede Eskalation ist ein Anruf. Angerufen wird:

- bei KRITISCH: sofort, spätestens 15 Minuten nach Eingang
- bei HOCH ab Stufe 2, also bei 80 Prozent verbrauchter Zusage
- bei jeder tatsächlichen Überschreitung, unabhängig von der Priorität, binnen
  30 Minuten nach Ablauf der Zusage
- wenn wir einen Termin vor Ort absagen oder verschieben müssen
- wenn eine Massnahme geplant ist, die den Betrieb des Kunden unterbricht

Angerufen wird der Ansprechpartner aus `04-kunden.md`, nicht der Melder. Der
Melder wird zusätzlich informiert, aber nachrangig. Kommt kein Kontakt
zustande, wird nach 15 Minuten erneut versucht und danach die Vertretung
angerufen.

## Was in der Eskalationsmail stehen muss

Betreff: `Eskalation Stufe N - <Ticketkennung> - <Kunde>`

Der Text hat sechs Punkte, in dieser Reihenfolge:

1. Ticketkennung, Kunde, Melder, Kategorie und Priorität
2. Was der Kunde merkt — in einem Satz, ohne Fachsprache
3. Wann gemeldet, wann die Zusage abläuft oder abgelaufen ist
4. Was bisher getan wurde, mit Zeitpunkten
5. Was jetzt konkret geplant ist, mit einem Zeitpunkt für den nächsten Schritt
6. Wer bis wann welche Entscheidung braucht

Empfänger: die Eskalationsstufe, die Teamleitung, der Kundenpate. Der Kunde
bekommt die Mail ab Stufe 2, im Wortlaut identisch — es gibt keine zweite
Fassung „für innen".

Vermutungen werden als Vermutung gekennzeichnet. Eine Eskalationsmail ohne
Punkt 5 gilt als unvollständig und wird zurückgewiesen.

## Rufbereitschaft

- Die Rufbereitschaft läuft werktags von 17:30 bis 07:30 und durchgehend am
  Wochenende und an Feiertagen.
- Sie wechselt wöchentlich, Übergabe montags um 08:00 im Teamgespräch.
- Reihenfolge im Turnus: mkr, jfe, sah, tre, nba. Der Turnus wird für ein
  ganzes Quartal im Voraus veröffentlicht.
- Rufbereitschaftsnummer: 04941 / 8 08 08-99. Sie ist auf das jeweilige
  Diensthandy geschaltet.
- Wer Rufbereitschaft hat, meldet sich innerhalb von 20 Minuten zurück und ist
  innerhalb von 60 Minuten arbeitsfähig, also am Rechner mit VPN.
- Ein Tausch ist erlaubt, muss aber vor Beginn der Woche schriftlich bei der
  Teamleitung angezeigt werden.

Angenommen werden ausserhalb der Servicezeit nur Meldungen der Priorität HOCH
und KRITISCH. Alles andere wird als Ticket erfasst und am nächsten Werktag
bearbeitet — der Anrufer bekommt diesen Satz ausdrücklich gesagt.

## Wochenenddienst

- Samstags von 09:00 bis 13:00 ist ein Bearbeiter im Haus. Das ist derselbe,
  der die Rufbereitschaft der laufenden Woche hat.
- Vos Logistik KG hat als einziger Kunde eine Nachtzusage: Meldungen aus der
  Nachtdisposition zwischen 22:00 und 06:00 werden angenommen und mit
  mindestens HOCH eingeordnet.
- Nordlicht Werften GmbH erreicht uns am Wochenende nur über die
  Werkfeuerwehr-Leitstelle, die dann bei uns anruft.
- Für alle anderen Kunden gibt es am Wochenende keinen aktiven Dienst,
  sondern nur die Rufbereitschaft für HOCH und KRITISCH.

## Übergabe

Jede Rufbereitschaft endet mit einer Übergabenotiz an das Team: welche
Tickets angefasst wurden, was offen blieb, was am Montag als Erstes
anzusehen ist. Die Notiz geht bis 08:30 an alle fünf Bearbeiter und an die
Teamleitung. Ohne Übergabenotiz gilt die Woche als nicht abgeschlossen.
