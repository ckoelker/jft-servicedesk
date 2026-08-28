package de.netzfactor.servicedesk.web;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Der ganze Weg eines Tickets ueber HTTP - gegen die echte Datenbank.
 *
 * <p>Seit Block 19 liegt die Ressource hinter einer Anmeldung. {@link TestSecurity}
 * setzt den angemeldeten Benutzer direkt, statt dass jeder Test erst durch das
 * Formular muesste - geprueft wird hier der Ticketablauf, nicht die Anmeldung.
 */
@QuarkusTest
@TestSecurity(user = "mara", roles = { "bearbeiter", "assistent" })
class TicketRessourceTest {

    private static final String TITEL = "VPN bricht nach zehn Minuten ab";

    // Firma und Melder stehen als Werte am Ticket, deshalb reicht der Rumpf ohne Stammdaten.
    private static final String NEUES_TICKET = """
            {"titel": "VPN bricht nach zehn Minuten ab",
             "beschreibung": "Nur fuer den Test angelegt.",
             "prioritaet": "HOCH",
             "kategorie": "NETZ",
             "firma": "Nordlicht Werften GmbH",
             "melder": "Anke Brehm"}
            """;

    @Test
    void einTicketWirdAngelegtGelesenErledigtKommentiertUndWiederGeloescht() {
        String kennung = given().contentType(ContentType.JSON).body(NEUES_TICKET)
                .when().post("/tickets")
                .then().statusCode(201)
                .extract().path("kennung");

        String titel = given()
                .when().get("/tickets/{kennung}", kennung)
                .then().statusCode(200)
                .extract().path("titel");
        assertThat(titel).isEqualTo(TITEL);

        JsonPath erledigt = given()
                .when().put("/tickets/{kennung}/status/ERLEDIGT", kennung)
                .then().statusCode(200)
                .extract().jsonPath();
        assertThat(erledigt.getString("status")).isEqualTo("ERLEDIGT");
        assertThat(erledigt.getString("erledigtAm")).isNotNull();

        given().contentType(ContentType.JSON)
                .body("""
                      {"text": "Router getauscht, laeuft wieder.", "autor": "Bernd Kappel"}
                      """)
                .when().post("/tickets/{kennung}/kommentare", kennung)
                .then().statusCode(201);

        List<String> autoren = given()
                .when().get("/tickets/{kennung}/kommentare", kennung)
                .then().statusCode(200)
                .extract().jsonPath().getList("autor", String.class);
        assertThat(autoren).containsExactly("Bernd Kappel");

        given().when().delete("/tickets/{kennung}", kennung).then().statusCode(204);
        given().when().get("/tickets/{kennung}", kennung).then().statusCode(404);
    }

    @Test
    void einTicketOhneTitelWirdAbgelehnt() {
        given().contentType(ContentType.JSON)
                .body(NEUES_TICKET.replace(TITEL, ""))
                .when().post("/tickets")
                .then().statusCode(400);
    }
}
