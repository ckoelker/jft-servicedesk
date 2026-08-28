package de.netzfactor.servicedesk.lager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Das ist {@code @Inject}, nachgebaut: ein Feld, das sich jemand anderes
 * fuellen laesst.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Braucht {
}
