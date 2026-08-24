# Ersatzteile und Lager

Das Lager steht im Erdgeschoss, Raum 004. Verantwortlich ist Tobias Renk
(tre), Vertretung Jonas Feld (jfe). Der Bestand wird nicht auf Zetteln
geführt, sondern ausschliesslich im Lagersystem.

## Das Lagersystem

- Erreichbar im Hausnetz unter `http://lager.intern:8082`.
- Jedes Teil hat eine Teilenummer, eine Bezeichnung, einen Bestand und einen
  Lagerort.
- Lagerorte sind die Regale 1 bis 8 im Raum 004. Ein Teil liegt immer im selben
  Regal; Umlagern ist nicht erlaubt.
- Der Bestand im System ist die Wahrheit. Wer etwas entnimmt, bucht es sofort
  aus — nicht am Ende des Tages.

## Wie die Teilenummern aufgebaut sind

Die Nummern laufen von **T-1001 bis T-1040**. Vergeben wird zyklisch über zehn
Warengruppen: Die **letzte Ziffer** der Nummer bestimmt die Warengruppe.

| Endziffer | Warengruppe |
|---|---|
| 1 | Tonerkassette |
| 2 | Netzteil 65 W |
| 3 | SFP-Modul |
| 4 | Tastatur DE |
| 5 | Dockingstation |
| 6 | Speichermodul 16 GB |
| 7 | Lüfter |
| 8 | Netzwerkkabel 3 m |
| 9 | Festplatte 1 TB |
| 0 | Monitorkabel HDMI |

Der Zehnerblock gibt die Ausführung an — Hersteller, Bauform oder Modellreihe:

| Block | Ausführung |
|---|---|
| T-1001 bis T-1010 | Ausführung 1 |
| T-1011 bis T-1020 | Ausführung 2 |
| T-1021 bis T-1030 | Ausführung 3 |
| T-1031 bis T-1040 | Ausführung 4 |

Beispiele: T-1005 ist eine Dockingstation der Ausführung 1, T-1025 eine
Dockingstation der Ausführung 3. T-1013 ist ein SFP-Modul der Ausführung 2.

Es gibt keine Nummern über T-1040. Alles, was nicht in diese vierzig Positionen
fällt — Einzugsrollen und Trennstege für Drucker, PoE-Injektoren, USB-C-Kabel,
Kartenleser —, ist kein Lagerteil und wird als Einzelbestellung beim
Hersteller ausgelöst.

## Meldebestand und Nachbestellung

- **Verschleissteile** (Tonerkassette, Netzteil, Tastatur, Netzwerkkabel,
  Monitorkabel, Lüfter): nachbestellen, sobald der Bestand auf **5** fällt.
- **Alle übrigen Teile**: nachbestellen, sobald der Bestand auf **3** fällt.
- Die Bestellmenge bringt den Bestand auf 12 bei Verschleissteilen und auf 6
  bei den übrigen.
- Der Lagerverantwortliche prüft dienstags und donnerstags jeweils um 09:00 die
  Liste der unterschrittenen Meldebestände.
- Ein Teil mit Bestand 0 wird sofort bestellt, unabhängig vom Prüftag, und im
  Ticket wird der Melder darüber informiert.

## Freigabe

| Auftragswert | Wer gibt frei |
|---|---|
| bis 250 Euro | der Bearbeiter selbst |
| über 250 bis 1.200 Euro | Teamleitung Mara Kruse |
| über 1.200 bis 5.000 Euro | Betriebsleitung Kirsten Vogt |
| über 5.000 Euro | Geschäftsführung Henning Brammer |

Die **Freigabegrenze von 250 Euro je Bestellung gilt pro Bearbeiter und Tag**;
zwei Bestellungen am selben Tag werden zusammengerechnet. Eine Bestellung darf
nicht geteilt werden, um unter der Grenze zu bleiben — das gilt als Verstoss
und wird der Teamleitung gemeldet.

Bestellungen, die an einen Kunden weiterberechnet werden, brauchen zusätzlich
die schriftliche Zustimmung des Ansprechpartners aus `04-kunden.md`,
unabhängig vom Betrag.

## Lieferzeiten

| Beschaffungsweg | Dauer |
|---|---|
| Entnahme aus dem eigenen Lager | sofort |
| Nachlieferung aus dem Zentrallager Emden | 1 Werktag |
| Bestellung beim Standardlieferanten | 3 Werktage |
| Herstellerbestellung, Nicht-Lagerteil | 10 Werktage |
| Sonderbestellung Fertigungsumgebung Nordlicht | 15 Werktage |

Ein Ticket, das auf eine Lieferung wartet, wird auf WARTET gesetzt. Damit
steht die Zusagezeit still (siehe `02-service-level.md`). Das voraussichtliche
Lieferdatum gehört in das Ticket, sonst gilt der Wartestatus als unbegründet.

## Leihgeräte

- Leihgeräte liegen nicht in den Regalen, sondern im Leihgeräteschrank neben
  der Tür, **Fach 14**. Dort liegen auch die Leihscheine.
- Ausgabe nur gegen Leihschein mit Ticketkennung, Name, Datum und
  Rückgabedatum. Ohne Leihschein wird nichts ausgegeben.
- Die Leihfrist beträgt **10 Werktage**. Sie kann einmal um 10 Werktage
  verlängert werden, wenn der Bearbeiter das im Ticket begründet.
- Wird ein Leihgerät nach 15 Werktagen nicht zurückgegeben, werden dem Kunden
  12 Euro je Kalendertag berechnet.
- Zurückgegebene Geräte werden zurückgesetzt, bevor sie wieder ins Fach
  kommen. Ein Gerät mit Kundendaten darf das Fach nicht erreichen.
- Die 48 Handscanner bei Vos Logistik KG sind dauerhafte Leihgeräte. Für sie
  gilt keine Frist, aber eine jährliche Zählung im Januar.

## Rückgabe defekter Teile

1. Defektes Teil mit der Ticketkennung beschriften.
2. In die rote Kiste im Regal 8 legen, nicht zurück ins Fach.
3. Im Lagersystem als defekt buchen — nicht einfach den Bestand erhöhen.
4. Teile in Garantie werden vom Lagerverantwortlichen gesammelt und einmal
   wöchentlich, freitags, zum Hersteller geschickt.
5. Datenträger werden nie zurückgeschickt. Festplatten aus der Warengruppe mit
   Endziffer 9 werden im Haus vernichtet und schriftlich quittiert.
