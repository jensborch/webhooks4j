package dk.jensborch.webhooks;

import static io.restassured.RestAssured.given;

import java.net.URI;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 *
 */
@QuarkusTest
public class RegisterTest {

    @Test
    public void testRegister() throws Exception {
        given()
                .when()
                .log().all()
                .body(new Webhook(new URI("http://localhost:8080/webhooks"), "test_topics"))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .post("consumer-webhooks")
                .then()
                .log().all()
                .statusCode(201);
    }

}
