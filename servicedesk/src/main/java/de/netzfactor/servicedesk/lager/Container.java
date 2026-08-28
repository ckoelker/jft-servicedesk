package de.netzfactor.servicedesk.lager;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Block 10: dependency injection in fuenfundzwanzig Zeilen.
 *
 * <p>Objekt bauen, Felder mit {@link Braucht} suchen, fuellen - und sich
 * merken, was schon gebaut ist. Mehr ist der Kern nicht.
 */
public final class Container {

    private final Map<Class<?>, Object> gebaut = new HashMap<>();

    public <T> T hole(Class<T> art) {
        Object vorhanden = gebaut.get(art);
        if (vorhanden != null) {
            return art.cast(vorhanden);
        }
        try {
            T objekt = art.getDeclaredConstructor().newInstance();
            // Erst merken, dann fuellen: sonst dreht sich ein Ring aus Abhaengigkeiten endlos.
            gebaut.put(art, objekt);
            for (Field feld : art.getDeclaredFields()) {
                if (feld.isAnnotationPresent(Braucht.class)) {
                    feld.setAccessible(true);
                    feld.set(objekt, hole(feld.getType()));
                }
            }
            return objekt;
        } catch (ReflectiveOperationException fehler) {
            throw new IllegalStateException(art.getName()
                    + " liess sich nicht bauen - gibt es einen Konstruktor ohne Argumente?", fehler);
        }
    }

    public static void main(String[] args) {
        Container container = new Container();

        System.out.println("Dafür muss das Lagersystem auf Port 8082 laufen.");
        Ersatzteilpruefer pruefer = container.hole(Ersatzteilpruefer.class);
        System.out.println(pruefer.pruefe("T-1001"));

        Ersatzteilpruefer nochmal = container.hole(Ersatzteilpruefer.class);
        System.out.println("Dasselbe Objekt beim zweiten Mal? " + (pruefer == nochmal));

        // Genau das macht CDI - nur mit Scopes, Proxies und Lebenszyklus statt einer HashMap.
    }
}
