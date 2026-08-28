package de.netzfactor.servicedesk.auswertung;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Ueberschrift und Reihenfolge einer Spalte im Bericht.
 *
 * <p>RUNTIME, weil sie erst zur Laufzeit gelesen wird - ohne das ist die
 * Annotation im fertigen Programm nicht mehr da. Sie steht an den Feldern des
 * records; die Reihenfolge ergibt sich aus der Reihenfolge der Komponenten.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.METHOD})
public @interface Spalte {

    /** Was im Kopf der Spalte steht. */
    String value();
}
