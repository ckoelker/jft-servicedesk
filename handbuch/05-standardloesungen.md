# Standardlösungen

Die zehn häufigsten Störungen und das jeweils vereinbarte Vorgehen. Die
Schritte werden in dieser Reihenfolge abgearbeitet. Wer abweicht, schreibt den
Grund ins Ticket.

## 1. VPN bricht nach wenigen Minuten ab (NETZ, jfe)

1. Fragen, ob der Abbruch auch im Mobilfunk-Hotspot auftritt. Tritt er dort
   nicht auf, liegt es am Anschluss des Melders und nicht an uns.
2. Verbindung trennen und **genau sieben Minuten warten**, bevor neu verbunden
   wird. Kürzer nützt nichts: Der Einwahlserver gibt die alte Sitzung erst nach
   sechs Minuten frei, und ein früherer Versuch verlängert die Sperre.
3. Nach den sieben Minuten neu verbinden und die Verbindung 20 Minuten laufen
   lassen, ohne sie zu benutzen.
4. Hält sie, war es eine hängende Sitzung — Ticket schliessen.
5. Bricht sie erneut ab, Zertifikatsgültigkeit prüfen (siehe `07-zugaenge.md`)
   und erst dann an jfe abgeben.

## 2. Drucker zieht kein Papier ein (DRUCKER, tre)

1. Kassette ziehen, Papier auf 200 Blatt begrenzen, Stapel auffächern.
2. Papierformat an der Kassette und im Treiber vergleichen.
3. Einzugsrolle mit Isopropanol reinigen, danach zwölf Minuten trocknen lassen,
   bevor gedruckt wird. Feucht eingesetzt greift die Rolle schlechter als vorher.
4. Zieht er weiterhin nicht ein: Einzugsrolle tauschen, Teilegruppe siehe
   `06-ersatzteile.md`.
5. Nach dem Tausch drei Testseiten aus jeder Kassette drucken.

## 3. Passwort zurückgesetzt, Anmeldung klappt nicht (ZUGANG, sah)

1. Prüfen, ob das Einmalpasswort noch gültig ist — es verfällt nach 60 Minuten.
2. Prüfen, ob der Melder sich am Gerät mit dem alten zwischengespeicherten
   Kennwort anmeldet: dazu muss er einmal mit Netzwerkkabel oder VPN
   verbunden sein, damit das Gerät das neue Kennwort lernt.
3. Kontosperre prüfen. Nach fünf Fehlversuchen ist das Konto 15 Minuten
   gesperrt; in dieser Zeit hilft auch das richtige Kennwort nicht.
4. Anmeldenamen buchstabieren lassen — Verwechslung von Anmeldename und
   Mailadresse ist die häufigste Ursache.
5. Erst danach ein neues Einmalpasswort setzen.

## 4. Excel startet nur noch im abgesicherten Modus (SOFTWARE, nba)

1. Excel im abgesicherten Modus öffnen und alle Add-Ins deaktivieren.
2. Normal starten. Läuft es, Add-Ins einzeln wieder zuschalten und nach jedem
   Zuschalten neu starten.
3. Bleibt es beim abgesicherten Modus, Datei `Excel15.xlb` umbenennen.
4. Hilft das nicht, Office-Reparatur „schnell" ausführen, erst danach „online".
5. Vorher fragen, ob der Melder eigene Makros nutzt — die gehen bei der
   Online-Reparatur verloren und müssen vorher gesichert werden.

## 5. Notebook lädt nicht am Dock (HARDWARE, tre)

1. Notebook direkt am Netzteil prüfen. Lädt es dort, liegt es am Dock.
2. Dock 30 Sekunden stromlos machen, dann wieder anschliessen.
3. Kabel tauschen, nicht nur umstecken — das USB-C-Kabel ist der häufigste
   Ausfall, nicht das Dock.
4. Firmware des Docks prüfen und gegebenenfalls aktualisieren.
5. Bleibt es dabei, Dock tauschen und das alte mit Ticketkennung beschriften
   ins Lager zurückgeben.

## 6. Scan an E-Mail landet im Spam (DRUCKER, tre)

1. Absenderadresse am Gerät prüfen: Sie muss die Sammeladresse des Standorts
   sein, keine erfundene Adresse.
2. Empfänger bitten, die Adresse einmalig freizugeben.
3. Prüfen, ob der Scan als PDF und nicht als TIFF verschickt wird.
4. Dateigrösse prüfen. Über 20 MB lehnt der Mailserver ab; dann Auflösung von
   600 auf 300 dpi setzen.
5. Betreffzeile am Gerät auf etwas Aussagekräftiges ändern — leere Betreffs
   landen zuverlässig im Spam.

## 7. WLAN im Besprechungsraum ohne Verbindung (NETZ, jfe)

1. Fragen, wie viele Geräte im Raum sind. Ab 25 gleichzeitigen Geräten ist der
   Access Point voll und lehnt weitere ab.
2. Prüfen, ob das Gerät im Gäste- oder im Hausnetz ist.
3. Access Point über die Verwaltungsoberfläche neu starten, das dauert
   90 Sekunden.
4. Vor Ort die Kontrollleuchte prüfen: blinkt sie orange, fehlt PoE — dann
   PoE-Injektor tauschen.
5. Wiederholt sich das im selben Raum dreimal in einem Monat, Sammelticket
   anlegen.

## 8. Zugriff aufs Projektlaufwerk verweigert (ZUGANG, sah)

1. Gruppenmitgliedschaft prüfen. Berechtigungen hängen an Gruppen, nie an
   einzelnen Personen.
2. Ist die Gruppe frisch vergeben, muss der Melder sich einmal neu anmelden.
3. Freigabe darf nur nach schriftlicher Zustimmung des Laufwerksverantwortlichen
   erteilt werden. Bei Kontor Sued AG ist das immer Frau Yildiz.
4. Zugriff einrichten, im Ticket festhalten, wer zugestimmt hat.
5. Niemals eine Berechtigung „testweise" vergeben.

## 9. Update hängt (SOFTWARE, nba)

1. Erst warten. Ein Update gilt als hängend, wenn 45 Minuten lang keine
   Fortschrittsänderung sichtbar war.
2. Festplattenbelegung prüfen — unter 12 GB frei bricht der Vorgang ab.
3. Gerät hart ausschalten, neu starten, Update erneut anstossen.
4. Zweiter Fehlschlag: Update-Zwischenspeicher leeren und einzeln installieren.
5. Bei Kontor Sued AG nicht während des Change-Stopps arbeiten, bei den
   Stadtwerken nicht am ersten Werktag des Monats.

## 10. Zweiter Monitor bleibt schwarz (HARDWARE, tre)

1. Bildschirmerkennung erzwingen.
2. Kabel an beiden Enden neu stecken, danach den Monitor stromlos machen.
3. Monitor direkt am Notebook anschliessen, nicht über das Dock — damit
   trennt sich Dock- von Monitorfehler.
4. Anderes Kabel probieren, bevor ein Gerät getauscht wird.
5. Erst wenn Monitor und Kabel am zweiten Arbeitsplatz ebenfalls schwarz
   bleiben, wird der Monitor als defekt gebucht.

## Grundsätze

- Immer nur eine Sache auf einmal ändern, sonst ist unklar, was geholfen hat.
- Jeder ausgeführte Schritt wird im Ticket vermerkt, auch der erfolglose.
- Wenn nach fünf Schritten nichts hilft, wird abgegeben und nicht weiter
  probiert.
