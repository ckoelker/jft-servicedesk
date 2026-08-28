package de.netzfactor.servicedesk.lager;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Das Lagersystem im Test durch ein Mock ersetzt - der Test laeuft ohne HTTP. */
class LagerauskunftTest {

    // Ein Fremdsystem im Test kostet Zeit und faellt aus; das Mock antwortet immer gleich.
    private final Lagerauskunft auskunft = Mockito.mock(Lagerauskunft.class);

    @Test
    void dasMockAntwortetStattDesLagersystems() {
        Mockito.when(auskunft.nach("T-1001"))
               .thenReturn(new Teil("T-1001", "Netzteil 150 W", 3, "Regal A3"));

        assertThat(auskunft.nach("T-1001").bestand()).isEqualTo(3);
        Mockito.verify(auskunft, Mockito.times(1)).nach("T-1001");
        Mockito.verifyNoMoreInteractions(auskunft);
    }

    @Test
    void beimAusfallDesLagersystemsBekommtDerAufruferDenGrundZuSehen() {
        Mockito.when(auskunft.nach("T-9999"))
               .thenThrow(new IllegalStateException("Lagersystem nicht erreichbar: http://localhost:8082"));

        assertThatThrownBy(() -> auskunft.nach("T-9999"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nicht erreichbar");
    }
}
