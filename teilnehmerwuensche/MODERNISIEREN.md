# Altes Java auf JDK 25 — was außer Handarbeit geht

Recherchestand 25.08.2026. Kurzfassung für ein Gespräch, keine Marktübersicht.

## Die Reihenfolge ist das Wichtigste

1. **Tests zuerst.** Ohne Netz merkt niemand, was die Automatik kaputt gemacht
   hat. Das ist der Punkt, der am häufigsten übersprungen wird.
2. **Build hochziehen**, nicht den Code: Maven/Gradle, Plugin-Versionen,
   `--release` statt `-source/-target`.
3. **Bestand aufnehmen** — `jdeps` (was hängt an JDK-Internas?), `jdeprscan`
   (was ist veraltet?). Erst jetzt weiß man, wie groß das Ding ist.
4. **Bibliotheken**, meist der eigentliche Engpass — `javax` → `jakarta`.
5. **Dann erst Idiome.** Das ist der Teil, der Spaß macht, und der unwichtigste.

## Wo die Werkzeuge angreifen

Das ist die Achse, an der man sie auseinanderhält — nicht „gut/schlecht":

| Werkzeug | Greift an | Ergebnis | Lizenz |
|---|---|---|---|
| **RefactorFirst** | Report | *wo anfangen?* God Classes, Zyklen, Churn aus der Git-Historie | Apache-2.0 |
| **MTA / kantra** (Red Hat) | Report | Inventar + Bewertung, Regeln in YAML | Apache-2.0 (Produkt: Abo) |
| **OpenRewrite** | Quelltext **und** Buildfiles | dauerhaft gepflegte Codebasis | **Achtung, s.u.** |
| **jSparrow** | Quelltext | dauerhaft gepflegte Codebasis | MIT + Commons Clause |
| **Tomcat Migration Tool** | fertige Artefakte | migriertes WAR, **kein** migriertes Repo | Apache-2.0 |
| **Eclipse Transformer** | fertige Artefakte | dito, generische Renaming-Maschine | EPL-2.0 / Apache-2.0 |
| **IntelliJ IDEA** | Quelltext, interaktiv | schnellster Weg für *ein* Projekt | kommerziell |

**Nur OpenRewrite und jSparrow hinterlassen eine Codebasis, die man weiterpflegt.**
Tomcat-Tool und Transformer sind Build-Schritte, die man dauerhaft mitschleppt,
wenn man den Quelltext nicht anfassen will.

## OpenRewrite: die Rezepte, die zählen

Geprüfte Namen (docs.openrewrite.org):

```
org.openrewrite.java.migrate.UpgradeToJava8 / …17 / …21 / …25
org.openrewrite.java.migrate.Java8ToJava11              ← heißt anders!
org.openrewrite.java.migrate.search.PlanJavaMigration   ← erst mal nur planen
org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta
```

Aufruf: `mvn rewrite:dryRun` (nur Patch) bzw. `rewrite:run` (ändert).

**Die Lizenzfalle:** Das Core-Framework ist Apache-2.0 — aber ausgerechnet
`rewrite-migrate-java`, das Modul mit den Java- *und* Jakarta-Rezepten, steht
unter der **Moderne Source Available License**. Eigener Code in der eigenen
Firma: erlaubt. Weiterverkauf, SaaS, Produkte darauf: nicht. Für Dienstleister
beim Kunden ist die Lage **nicht geklärt** — Lizenztext lesen, nicht raten.

Die Doku lenkt inzwischen auf ein authentifiziertes Repository um; auf Maven
Central ist `rewrite-migrate-java:3.42.1` derzeit noch auflösbar.

**Die Voraussetzung, an der es bei einem Java-5-Projekt scheitert:**
OpenRewrite braucht einen Build, der durchläuft. Es kann nicht auf etwas
ansetzen, das sich nicht übersetzen lässt. Deshalb steht Schritt 2 oben vor
Schritt 5.

## Der kostenlose Weg: die IDE

Bevor man ein Werkzeug einkauft — beide großen IDEs haben das eingebaut, nach
Sprachversion gruppiert.

**Der Hebel, ohne den nichts passiert: erst den Language Level hochziehen.**
Die Inspektionen richten sich danach. Solange das Modul auf 5 steht, meldet
keine von ihnen etwas, und man hält die IDE für nutzlos.

| | IntelliJ IDEA | Eclipse |
|---|---|---|
| Wo | Inspections → Java → **Java language level migration aids** | `Source > Clean Up…`, Reiter **Java Feature** |
| Umfang | Java 5 bis 26 | Java 5 bis 25, aber lückenhaft |
| Records | ja, im Batch | nur einzeln, seit 2026-03 |
| Streams, sealed | ja | **nein** |
| Ganzes Projekt | `Code | Inspect Code…` | Clean Up auf dem Projekt |
| Eine Regel, ganzes Projekt | `Run Inspection by Name` (Ctrl+Alt+Shift+I) | über das Profil |
| Fixes im Rutsch | `Code | Code Cleanup` | der Clean-Up-Wizard, mit Vorschau |
| Profil teilbar | ja, `.idea/inspectionProfiles` | ja, als XML |
| Headless / CI | `idea inspect`, dazu Qodana | **nur der Formatter** |

Für den Java-5-Bestand ist das der praktikabelste Einstieg: Level auf 8 setzen,
`Run Inspection by Name` je Regel über das Projekt, Diff ansehen, committen,
nächste Regel. Eine Regel je Commit — das ist im Review noch prüfbar.

**Hausinterne Altlasten** (eigene Utility-Klasse → JDK-Äquivalent) gehen mit
*Structural Search and Replace*: Template bauen, per **Create Inspection from
Template** zur Inspektion mit Quick-Fix machen, dann im Batch laufen lassen.
Dafür braucht es kein OpenRewrite-Rezept.

## Alt → Neu, und wer es automatisch macht

| Alt | Neu | Automatisch? |
|---|---|---|
| rohe Typen | Generics | IDE-Inspektion, teils OpenRewrite |
| anonyme Klasse | Lambda / Methodenreferenz | ja, alle Refactoring-Werkzeuge |
| `Iterator`-Schleife | for-each, dann Stream | ja (Stream: mit Vorsicht) |
| `StringBuffer` | `StringBuilder` | ja |
| `new Integer(…)` | `Integer.valueOf` | ja |
| `Vector` / `Hashtable` | `ArrayList` / `HashMap` | ja — **Semantik prüfen!** |
| `Arrays.asList` | `List.of` | ja — **nicht identisch** (unveränderlich, kein `null`) |
| try-finally | try-with-resources | ja |
| `instanceof` + Cast | Pattern Matching | ja |
| Datenklasse | `record` | halb — Werkzeug schlägt vor, Entscheidung bleibt |
| `Date` / `Calendar` | `java.time` | **nein**, das ist Handarbeit |
| `Thread` | `ExecutorService`, virtual threads | **nein** |

Die Faustregel: Je mechanischer, desto zuverlässiger. Alles, wo sich das
*Verhalten* ändern kann, will einen Menschen — und die Tests aus Schritt 1.

## Fallstricke ohne Compilerfehler

Die teuren Fehler sind die, bei denen alles übersetzt und sich trotzdem etwas
ändert:

- **UTF-8 ist ab Java 18 Standard** für Dateien. Wer sich auf die
  Plattformkodierung verlassen hat, merkt es an kaputten Umlauten.
- **CLDR statt JRE-Locale-Daten** ab Java 9 — Datums- und Zahlenformate sehen
  anders aus.
- **Starke Kapselung der JDK-Internas** ab 16/17, `--illegal-access` ist weg.
- `sun.misc.Unsafe`, `SecurityManager`, `finalize()` — weg oder auf dem Weg.
- JAXB/JAX-WS sind seit Java 11 draußen und müssen als Abhängigkeit zurück.

## Ehrlich zu LLMs

Deterministische Werkzeuge und LLMs lösen verschiedene Probleme.

| | Rezept-Engine | LLM |
|---|---|---|
| Gleicher Input, gleicher Output | ja | nein |
| Prüfbar im Review | Diff je Regel | nur der Diff selbst |
| Kennt dein Projekt | nein | teilweise |
| Kann den Einzelfall | nein | ja |

Der sinnvolle Schnitt: **Masse maschinell, Einzelfälle mit Kopf.** Erst
OpenRewrite über die 10 000 mechanischen Stellen laufen lassen, dann den Rest
ansehen. Wer umgekehrt anfängt, prüft 10 000 Diffs von Hand.

Red Hat geht in MTA denselben Weg: `kantra transform openrewrite` delegiert die
Quelltextänderung an OpenRewrite, die LLM-Unterstützung („Developer
Lightspeed") ist ein separater, kostenpflichtiger Aufsatz.

## Nicht belegt

- **„Freud"** als Java-Modernisierungswerkzeug existiert nach vier Suchläufen
  nicht. Vermutlich eine Namensverwechslung.
- jSparrow: Regeln reichen nur **bis Java 16**, das Maven-Plugin braucht
  weiterhin eine Lizenz auf Anfrage. „Open Source" seit 4.20.0 stimmt nicht
  ganz — MIT **mit Commons Clause** ist nicht OSI-konform.
- Ob `mta-cli analyze` bei Funden einen Exit-Code ≠ 0 liefert, steht nirgends —
  also nicht darauf bauen, damit einen Build zu brechen.

Nicht mehr recherchiert (Abbruch): AWS Transform for Java, Error Prone /
Refaster, SonarQube-Modernisierungsregeln, Renovate/Dependabot.
