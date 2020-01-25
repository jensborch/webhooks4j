package dk.jensborch.webhooks;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import dk.jensborch.webhooks.publisher.WebhookPublisher;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 *
 */
@QuarkusTest
public class RegisterTest {

    @Inject
    TestEventListener listener;

    @Inject
    WebhookPublisher publisher;

    @Test
    public void testRegister() throws Exception {
        String location = given()
                .when()
                .log().all()
                .body(new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/receive-callback"), TestEventListener.TOPIC))
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
        Map<String, Object> data = new HashMap<>();
        publisher.publish(new WebhookEvent(TestEventListener.TOPIC, data));
        assertEquals(1, listener.getCount());
    }

}
