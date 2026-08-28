package de.netzfactor.servicedesk.auswertung;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Derselbe Schreiber fuer jeden Bericht - er liest die Spalten zur Laufzeit vom record ab. */
class BerichtsschreiberTest {

    private static final List<Zeilen.Auslastung> DATEN = List.of(
            new Zeilen.Auslastung("Anke Brehm", 3L, 7.5),
            new Zeilen.Auslastung("Bernd Kappel", 1L, 2.0));

    @Test
    void derKopfKommtAusDenSpaltenAnnotationenInReihenfolgeDerKomponenten() {
        assertThat(Berichtsschreiber.kopf(Zeilen.Auslastung.class))
                .containsExactly("Bearbeiter", "Tickets", "Stunden");
    }

    @Test
    void dieZeilenLiefernDieWerteInDerselbenReihenfolge() {
        assertThat(Berichtsschreiber.zeilen(DATEN))
                .containsExactly(List.of("Anke Brehm", 3L, 7.5),
                                 List.of("Bernd Kappel", 1L, 2.0));
    }

    @Test
    void derTextberichtZeigtUeberschriftUndWerte() {
        assertThat(Berichtsschreiber.alsText("Auslastung der Bearbeiter", DATEN))
                .contains("Auslastung der Bearbeiter")
                .contains("Bearbeiter")
                .contains("Anke Brehm");
    }

    @Test
    void excelUndPdfTragenIhreSignaturAmAnfang() {
        // xlsx ist ein ZIP, deshalb PK; ein PDF beginnt immer mit %PDF.
        assertThat(Berichtsschreiber.alsExcel("Auslastung", DATEN))
                .startsWith((byte) 'P', (byte) 'K');
        assertThat(Berichtsschreiber.alsPdf("Auslastung", DATEN))
                .startsWith((byte) '%', (byte) 'P', (byte) 'D', (byte) 'F');
    }
}
