# Zugänge, Passwörter, Zwei-Faktor

Zugänge sind der empfindlichste Teil unserer Arbeit. Die Kategorie ZUGANG hat
die kürzeste Zusage von allen — vier Stunden — und gleichzeitig die
strengsten Regeln.

## Passwörter

- Mindestens 14 Zeichen. Keine Vorgabe zu Sonderzeichen, dafür keine
  Wiederverwendung der letzten fünf Kennwörter.
- Kein turnusmässiger Zwangswechsel. Gewechselt wird bei Verdacht auf
  Offenlegung, nach einem Zurücksetzen und beim Verlassen eines Projekts mit
  geteilten Zugängen.
- Nach fünf Fehlversuchen sperrt das Konto für 15 Minuten. Die Sperre kann
  nicht vorzeitig aufgehoben werden.
- Ein Einmalpasswort gilt **60 Minuten** und muss bei der ersten Anmeldung
  geändert werden.
- Geteilte Konten sind verboten. Wo ein Funktionskonto nötig ist, wird es
  benannt, dokumentiert und einer Person zugeordnet.

## Identitätsprüfung vor dem Zurücksetzen

Bevor ein Kennwort zurückgesetzt wird, muss feststehen, wer anruft. Es gilt
immer der Rückruf:

1. Ticket anlegen, Melder benennen lassen.
2. Auflegen und die im Personalverzeichnis des Kunden hinterlegte Nummer
   zurückrufen. Nicht die Nummer, die der Anrufer nennt.
3. Zwei Angaben abfragen, die nicht auf der Visitenkarte stehen: Kostenstelle,
   Vorgesetzter, Gerätenummer.
4. Erst danach zurücksetzen.

Bei Kontor Sued AG genügt der Rückruf nicht. Dort braucht jedes Zurücksetzen
zusätzlich eine Mail von Elif Yildiz oder Frank Obermeier.

## Was niemals per Telefon herausgegeben wird

Es gibt keine Ausnahme, auch nicht bei Priorität KRITISCH, auch nicht auf
Bitte einer Führungskraft:

- Passwörter und Einmalpasswörter im Klartext
- Zwei-Faktor-Codes und Ersatzcodes
- das Kennwort einer VPN-Zertifikatsdatei
- Wiederherstellungscodes für Verschlüsselung
- PIN oder PUK von Karten und Konnektoren

Einmalpasswörter gehen ausschliesslich an die dienstliche Mailadresse oder
werden dem Vorgesetzten übergeben, der sie persönlich weitergibt. Wer am
Telefon zu einer dieser Angaben gedrängt wird, bricht das Gespräch ab und
meldet den Vorfall der Teamleitung — auch dann, wenn nichts herausgegeben
wurde.

## Zwei-Faktor

- Zweiter Faktor ist eine Einmalcode-App auf dem dienstlichen Telefon.
  SMS-Codes werden nicht mehr eingerichtet.
- Beim Einrichten bekommt jeder zehn Ersatzcodes. Jeder Code gilt genau einmal.
- Neu registriert wird ein Gerät nur nach Identitätsprüfung per Videogespräch
  oder persönlich beim Kundenpaten vor Ort. Ein Telefonat reicht dafür nicht.
- Ist das Telefon verloren, wird der zweite Faktor sofort gesperrt und das
  Ticket auf HOCH gesetzt.
- Zwei-Faktor ist Pflicht für VPN, Postfach und alle Zugänge von ausserhalb.
  Innerhalb des Hausnetzes an einem verwalteten Gerät entfällt der zweite
  Faktor.

## VPN

- Jeder Zugang hängt an einem persönlichen Zertifikat mit **12 Monaten**
  Gültigkeit.
- 30 Tage vor Ablauf geht automatisch eine Erinnerung an den Nutzer und ein
  Ticket der Kategorie NETZ an den Fachpaten.
- Ein abgelaufenes Zertifikat wird nicht verlängert, sondern neu ausgestellt.
- Zertifikate werden nie per Mail verschickt, sondern über das
  Selbstbedienungsportal abgeholt.
- Gleichzeitige Anmeldung von zwei Geräten mit demselben Zertifikat ist nicht
  möglich. Die ältere Sitzung wird getrennt und ist erst nach sechs Minuten
  wieder aufbaubar (siehe `05-standardloesungen.md`).
- Für die Fertigungsnetze der Nordlicht Werften GmbH gibt es keinen
  VPN-Zugang. Arbeiten dort finden vor Ort statt.

## Eintritt eines Mitarbeiters

| Zeitpunkt | Was passiert |
|---|---|
| 5 Werktage vorher | Antrag des Kunden liegt schriftlich vor, mit Rolle und Vorgesetztem |
| 3 Werktage vorher | Gerät wird vorbereitet, Ersatzteile und Zubehör gebucht |
| 2 Werktage vorher | Konto und Postfach werden angelegt, noch gesperrt |
| erster Arbeitstag, 08:00 | Freischaltung, Übergabe des Einmalpassworts an den Vorgesetzten |
| erster Arbeitstag | Einrichtung des zweiten Faktors gemeinsam mit dem Nutzer |

Ohne schriftlichen Antrag wird kein Konto angelegt — auch nicht „schon mal
vorbereitend". Kommt der Antrag später als fünf Werktage vorher, wird das
Konto trotzdem angelegt, aber die Zusage von vier Stunden gilt dann nicht.

## Austritt eines Mitarbeiters

| Zeitpunkt | Was passiert |
|---|---|
| letzter Arbeitstag, 17:00 | Alle Zugänge werden gesperrt, VPN-Zertifikat wird zurückgezogen |
| letzter Arbeitstag | Rückgabe von Gerät, Telefon, Karte und Leihgeräten wird quittiert |
| + 1 Werktag | Postfach wird auf den Vorgesetzten weitergeleitet |
| + 90 Tage | Weiterleitung endet, Postfach wird archiviert |
| + 180 Tage | Konto wird gelöscht, Archiv bleibt bestehen |

Bei einer Trennung im Streit sperrt der Kunde selbst den Zeitpunkt: Auf
schriftliche Anforderung des Ansprechpartners sperren wir sofort, nicht erst
um 17:00. Solche Aufträge werden als KRITISCH geführt.

## Ablauf bei einem Verdachtsfall

1. Konto sofort sperren, nicht erst Rücksprache halten.
2. Ticket mit Priorität KRITISCH anlegen, Kategorie ZUGANG.
3. Teamleitung und Geschäftsführung parallel informieren
   (siehe `03-eskalation.md`).
4. Anmeldeprotokolle der letzten 30 Tage sichern, bevor irgendetwas geändert
   wird.
5. Erst danach mit dem Nutzer sprechen.
