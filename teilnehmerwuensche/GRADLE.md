# Und wenn wir Gradle benutzen?

**Kurz: Quarkus kann Gradle vollwertig.** Es gibt einen offiziellen
Gradle-Plugin, `code.quarkus.io` liefert Gradle-Projekte, und kein
Quarkus-Feature fehlt dort.

Im Kurs bleiben wir trotzdem bei Maven — nicht weil es besser wäre, sondern
damit alle dieselbe Kommandozeile haben. Der Stoff der Woche ist Java, nicht
das Build-Werkzeug.

## Dieselben Befehle

| | Maven | Gradle |
|---|---|---|
| Entwicklungsmodus | `./mvnw quarkus:dev` | `./gradlew quarkusDev` |
| Bauen | `./mvnw package` | `./gradlew build` |
| Tests | `./mvnw test` | `./gradlew test` |
| Extension hinzufügen | `./mvnw quarkus:add-extension -Dextensions=…` | `./gradlew addExtension --extensions=…` |
| Integrationstests | `./mvnw verify` | `./gradlew quarkusIntTest` |

Voraussetzung ist Gradle 9.6 oder der mitgelieferte Wrapper `./gradlew`.

## Die drei Stellen, an denen es sich unterscheidet

**1 — Der Plugin statt des Maven-Plugins.** In Gradle wird er oben deklariert,
nicht unten im `<build>`:

```groovy
plugins {
    id 'java'
    id 'io.quarkus'
}
```

**2 — Die BOM heißt `enforcedPlatform`.** Das ist das Gegenstück zum
`<scope>import</scope>` in der `dependencyManagement`-Sektion:

```groovy
dependencies {
    implementation enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}")
    implementation 'io.quarkus:quarkus-rest-jackson'
    implementation 'io.quarkus:quarkus-hibernate-orm-panache'
}
```

**3 — Extensions ohne Versionsnummer.** Genau wie bei Maven: Die BOM legt sie
fest. Das ist der Punkt aus B10, und er gilt hier unverändert.

Die Platform-Koordinaten stehen in der `gradle.properties` statt in den
`<properties>` der `pom.xml`. Ein Fortschrittsbalken wie unsere `pom.xml`
ließe sich genauso bauen — Kommentarzeichen weg, Zeile aktiv.

## Zwei Einschränkungen, die man kennen sollte

**Continuous testing sieht nüchterner aus.** Gradle läuft als Daemon, und
Quarkus kann darin die hübsche Testausgabe nicht zeichnen — es fällt auf
normales Logging zurück. Funktional identisch, optisch schlechter.

**`quarkusDev` verträgt sich nicht mit Gradles configuration cache.** Wer den
global eingeschaltet hat, bekommt einen Fehler, der nichts mit dem eigenen Code
zu tun hat. Dasselbe gilt für `quarkusRun` und die Extension-Befehle. Wenn der
Entwicklungsmodus grundlos klemmt: **hier zuerst nachsehen.**

Dazu eine Kleinigkeit für später: Native Tests laufen immer im Profil `prod`,
ein eigenes Testprofil gibt es dort nicht.

## Wenn ihr umstellen wollt

**Nicht die `pom.xml` von Hand übersetzen.** Lasst das Projekt neu erzeugen und
zieht euren Quelltext hinüber:

```bash
quarkus create app de.netzfactor:servicedesk --gradle
```

Oder auf [code.quarkus.io](https://code.quarkus.io) bei *Build Tool* auf Gradle
stellen. Der Generator kennt die richtigen Koordinaten, den Wrapper und die
Verzeichnisstruktur — von Hand übersetzt man sich genau daran wund.

Der Aufwand für ein Projekt dieser Größe ist etwa eine Stunde, und danach
funktioniert alles aus dieser Woche unverändert.

## Fazit

Die Wahl des Build-Werkzeugs ändert an dem, was du in dieser Woche über Java
lernst, keine Zeile. Nehmt das, was bei euch im Haus ohnehin läuft.
