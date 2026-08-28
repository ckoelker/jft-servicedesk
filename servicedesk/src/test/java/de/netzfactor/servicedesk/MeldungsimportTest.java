package de.netzfactor.servicedesk;

import de.netzfactor.servicedesk.dto.Ergebnis;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.dto.Meldung;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Ein Import, der kaputte Zeilen mittraegt statt abzubrechen. */
class MeldungsimportTest {

    // Die CSV steht im Test, nicht in den Ressourcen: so ist der Fehlerfall sichtbar.
    private static final String CSV = """
            firma;melder;titel;prioritaet
            Nordlicht Werften GmbH;Anke Brehm;Kran 3 meldet Stoercode E17;HOCH
            Stadtwerke Aurich;Carla Osei;Zaehlerauslesung bricht nachts ab;DRINGEND
            Kontor Sued AG;Elif Yildiz;Rechnungslauf haengt in der Freigabe
            Vos Logistik KG;Hendrik Vos;Barcodescanner verliert die Kopplung;NORMAL
            """;

    @Test
    void kaputteZeilenWerdenZuWertenStattZumAbbruch() {
        List<Ergebnis<Meldung>> ergebnisse =
                Meldungsimport.lies(new BufferedReader(new StringReader(CSV)));

        assertThat(ergebnisse).hasSize(4);
        assertThat(ergebnisse).filteredOn(Ergebnis.Gelungen.class::isInstance).hasSize(2);
        assertThat(ergebnisse).filteredOn(Ergebnis.Misslungen.class::isInstance).hasSize(2);
    }

    @Test
    void jederGrundNenntSeineZeilennummer() {
        List<Ergebnis<Meldung>> ergebnisse =
                Meldungsimport.lies(new BufferedReader(new StringReader(CSV)));

        assertThat(ergebnisse.get(1)).isInstanceOfSatisfying(Ergebnis.Misslungen.class,
                fehler -> assertThat(fehler.grund()).contains("Zeile 3").contains("DRINGEND"));
        assertThat(ergebnisse.get(2)).isInstanceOfSatisfying(Ergebnis.Misslungen.class,
                fehler -> assertThat(fehler.grund()).contains("Zeile 4").contains("3 Felder"));
    }

    @Test
    void dieGutenZeilenBehaltenIhreWerte() {
        List<Ergebnis<Meldung>> ergebnisse =
                Meldungsimport.lies(new BufferedReader(new StringReader(CSV)));

        assertThat(Meldungsimport.ersteGute(ergebnisse))
                .contains(new Meldung("Nordlicht Werften GmbH", "Anke Brehm",
                                      "Kran 3 meldet Stoercode E17", Prioritaet.HOCH));
        assertThat(ergebnisse.get(3)).isInstanceOfSatisfying(Ergebnis.Gelungen.class,
                gut -> assertThat(gut.wert()).isEqualTo(
                        new Meldung("Vos Logistik KG", "Hendrik Vos",
                                    "Barcodescanner verliert die Kopplung", Prioritaet.NORMAL)));
    }
}
