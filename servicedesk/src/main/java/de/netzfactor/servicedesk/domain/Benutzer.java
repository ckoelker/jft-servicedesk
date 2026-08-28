package de.netzfactor.servicedesk.domain;

import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Entity;

/**
 * Wer sich anmelden darf - eine gewoehnliche Panache-Entity mit vier Feldern.
 *
 * <p>Erst {@link UserDefinition} macht daraus die Benutzerverwaltung: Quarkus
 * baut daraus zur Bauzeit den Zugriff, der beim Anmelden befragt wird. Es gibt
 * deshalb keine Klasse, die Benutzer und Passwort selbst vergleicht - genau das
 * ist der Punkt.
 *
 * <p>Die beiden Benutzer kommen aus <code>datenbank/02-daten.sql</code> und
 * werden beim ersten Start des Containers angelegt.
 */
@Entity
@UserDefinition
public class Benutzer extends Basis {

    @Username
    public String benutzername;

    /**
     * Klartext, und das ist hier Absicht: in einer Vorfuehrung soll man in der
     * SQL-Datei sehen koennen, womit man sich anmeldet. Ohne diese Angabe
     * erwartet die Erweiterung einen bcrypt-Hash im Modular Crypt Format - und
     * das waere fuer eine echte Anwendung auch die richtige Wahl.
     */
    @Password(PasswordType.CLEAR)
    public String passwort;

    /**
     * Mehrere Rollen durch Komma getrennt in einer Spalte. Die Erweiterung
     * zerlegt das Feld selbst; eine eigene Rollentabelle waere hier nur eine
     * Beziehung mehr ohne Erkenntnisgewinn.
     */
    @Roles
    public String rollen;

    /** Der Name, der oben auf der Seite steht - fuer die Anmeldung ohne Bedeutung. */
    public String anzeigename;

    public static Benutzer nach(String benutzername) {
        return find("benutzername", benutzername).firstResult();
    }
}
