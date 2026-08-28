package de.netzfactor.servicedesk.auswertung;

import de.netzfactor.servicedesk.domain.Kategorie;
import de.netzfactor.servicedesk.domain.Prioritaet;
import de.netzfactor.servicedesk.domain.Status;
import de.netzfactor.servicedesk.domain.Ticket;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** Die SLA-Regel fuer sich allein: kein Framework, keine Datenbank, nur Werte. */
class SlaTest {

    // Feste Zeitpunkte statt now(): sonst haengt das Ergebnis am Tag des Testlaufs.
    private static final LocalDateTime MONTAG_NEUN_UHR = LocalDateTime.of(2026, 3, 2, 9, 0);

    // Der Kalender ist ein Interface mit einer Methode - im Test genuegt ein Lambda.
    private static final Kalender DURCHGEHEND_ARBEITSTAGE = tag -> false;

    @Test
    void hoeherePrioritaetVerkuerztDieZusage() {
        assertThat(Sla.frist(ticket(Prioritaet.NIEDRIG, Kategorie.NETZ, null), DURCHGEHEND_ARBEITSTAGE))
                .isEqualTo(Duration.ofHours(16));
        assertThat(Sla.frist(ticket(Prioritaet.NORMAL, Kategorie.NETZ, null), DURCHGEHEND_ARBEITSTAGE))
                .isEqualTo(Duration.ofHours(8));
        assertThat(Sla.frist(ticket(Prioritaet.HOCH, Kategorie.NETZ, null), DURCHGEHEND_ARBEITSTAGE))
                .isEqualTo(Duration.ofHours(4));
        assertThat(Sla.frist(ticket(Prioritaet.KRITISCH, Kategorie.NETZ, null), DURCHGEHEND_ARBEITSTAGE))
                .isEqualTo(Duration.ofHours(2));
    }

    @Test
    void einFeiertagMittendrinVerlaengertDieFristUmGenauEinenTag() {
        Ticket ticket = ticket(Prioritaet.NORMAL, Kategorie.SOFTWARE, null);
        Kalender mitFeiertag = tag -> tag.equals(LocalDate.of(2026, 3, 3));

        assertThat(Sla.frist(ticket, mitFeiertag))
                .isEqualTo(Sla.frist(ticket, DURCHGEHEND_ARBEITSTAGE).plusDays(1));
        assertThat(Sla.faelligAm(ticket, mitFeiertag))
                .isEqualTo(LocalDateTime.of(2026, 3, 5, 9, 0));
    }

    @Test
    void rechtzeitigErledigtBleibtInDerZusage() {
        Ticket ticket = ticket(Prioritaet.NORMAL, Kategorie.NETZ, LocalDateTime.of(2026, 3, 2, 16, 0));

        assertThat(Sla.faelligAm(ticket, DURCHGEHEND_ARBEITSTAGE))
                .isEqualTo(LocalDateTime.of(2026, 3, 2, 17, 0));
        assertThat(Sla.inDerZusage(ticket, DURCHGEHEND_ARBEITSTAGE, MONTAG_NEUN_UHR)).isTrue();
    }

    @Test
    void zuSpaetErledigtReisstDieZusage() {
        Ticket ticket = ticket(Prioritaet.NORMAL, Kategorie.NETZ, LocalDateTime.of(2026, 3, 2, 18, 0));

        assertThat(Sla.inDerZusage(ticket, DURCHGEHEND_ARBEITSTAGE, MONTAG_NEUN_UHR)).isFalse();
    }

    // Das Ticket ist eine Entity mit offenen Feldern - im Test genuegt es, sie zu setzen.
    private static Ticket ticket(Prioritaet prioritaet, Kategorie kategorie, LocalDateTime erledigtAm) {
        Ticket ticket = new Ticket();
        ticket.kennung = "S-0001";
        ticket.titel = "Kran 3 meldet Stoercode E17";
        ticket.firma = "Nordlicht Werften GmbH";
        ticket.melder = "Anke Brehm";
        ticket.bearbeiter = "Bernd Kappel";
        ticket.kategorie = kategorie;
        ticket.prioritaet = prioritaet;
        ticket.status = erledigtAm == null ? Status.NEU : Status.ERLEDIGT;
        ticket.gemeldetAm = MONTAG_NEUN_UHR;
        ticket.erledigtAm = erledigtAm;
        return ticket;
    }
}
