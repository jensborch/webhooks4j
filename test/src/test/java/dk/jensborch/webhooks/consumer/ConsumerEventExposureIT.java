package dk.jensborch.webhooks.consumer;

import static io.restassured.RestAssured.given;

import java.net.URI;
import java.util.HashMap;

import dk.jensborch.webhooks.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 * Integration test for
 * {@link dk.jensborch.webhooks.consumer.ConsumerEventExposur}
 */
@QuarkusTest
public class ConsumerEventExposureIT {

    @Test
    public void testRegisterWebhook() throws Exception {
        String location = given()
                .when()
                .log().all()
                .body(new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), this.getClass().getName()))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .post("consumer-webhooks")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .header("location");

        given()
                .when()
                .log().all()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .get(location)
                .then()
                .log().all()
                .statusCode(200);
    }

    @Test
    public void testPublishEvent() throws Exception {
        given()
                .when()
                .log().all()
                .body(new WebhookEvent("test_topic2", new HashMap<>()))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .post("consumer-events")
                .then()
                .log().all()
                .statusCode(200);
    }
}
