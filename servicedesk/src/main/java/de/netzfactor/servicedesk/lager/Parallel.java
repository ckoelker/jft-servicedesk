package de.netzfactor.servicedesk.lager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/**
 * Block 15: zwanzig Teile beim Lagersystem abfragen - dreimal dieselbe Arbeit.
 *
 * <p>Das Lagersystem antwortet auf jede Anfrage erst nach 300 ms. Wie lange der
 * ganze Lauf dauert, entscheidet daher allein, wie viele Anfragen gleichzeitig
 * unterwegs sein duerfen.
 */
public final class Parallel {

    private static final List<String> NUMMERN =
            IntStream.rangeClosed(1001, 1020).mapToObj(n -> "T-" + n).toList();

    private Parallel() {
    }

    public static void main(String[] args) {
        Lagerauskunft auskunft = new Lagerauskunft();
        try {
            auskunft.nach(NUMMERN.get(0));
        } catch (RuntimeException fehler) {
            System.out.println("Das Lagersystem antwortet nicht — bitte in "
                    + "jft-servicedesk/lagersystem `./mvnw quarkus:dev` starten.");
            System.exit(1);
        }

        long nacheinander = nacheinander(auskunft);
        long pool = mitDienst(auskunft, Executors.newFixedThreadPool(4));
        long virtuell = mitDienst(auskunft, Executors.newVirtualThreadPerTaskExecutor());

        System.out.println();
        System.out.printf("%d Teile beim Lagersystem, je 300 ms Antwortzeit%n", NUMMERN.size());
        System.out.println("--------------------------------------------------");
        System.out.printf("%-32s %8d ms%n", "nacheinander", nacheinander);
        System.out.printf("%-32s %8d ms%n", "newFixedThreadPool(4)", pool);
        System.out.printf("%-32s %8d ms%n", "newVirtualThreadPerTaskExecutor", virtuell);
    }

    private static long nacheinander(Lagerauskunft auskunft) {
        long start = System.nanoTime();
        List<Teil> teile = new ArrayList<>();
        for (String nummer : NUMMERN) {
            teile.add(auskunft.nach(nummer));
        }
        return millisekunden(start, teile.size());
    }

    // Die dritte Zeile beweist, dass zwanzig blockierende Aufrufe nicht mehr
    // kosten als einer, sobald kein Thread mehr knapp ist - und am Quelltext der
    // Aufgabe ist dafuer nichts anders als beim Pool, nur die Zeile mit Executors.
    private static long mitDienst(Lagerauskunft auskunft, ExecutorService dienst) {
        long start = System.nanoTime();
        List<Future<Teil>> offen = new ArrayList<>();
        try (dienst) {
            for (String nummer : NUMMERN) {
                offen.add(dienst.submit(() -> auskunft.nach(nummer)));
            }
        }
        return millisekunden(start, hole(offen).size());
    }

    private static List<Teil> hole(List<Future<Teil>> offen) {
        List<Teil> teile = new ArrayList<>();
        try {
            for (Future<Teil> ergebnis : offen) {
                teile.add(ergebnis.get());
            }
        } catch (InterruptedException fehler) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Abruf wurde unterbrochen", fehler);
        } catch (ExecutionException fehler) {
            throw new IllegalStateException("Ein Abruf ist fehlgeschlagen", fehler.getCause());
        }
        return teile;
    }

    private static long millisekunden(long start, int anzahl) {
        if (anzahl != NUMMERN.size()) {
            throw new IllegalStateException("Es kamen nur " + anzahl + " Teile zurück");
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}
