package dk.jensborch.webhooks;

import static io.restassured.RestAssured.given;

import java.util.HashMap;

import javax.inject.Inject;

import dk.jensborch.webhooks.publisher.WebhookPublisher;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

/**
 *
 */
@QuarkusTest
public class PublisherTest {
    
    @Inject
    TestEventListener listener;
    
    @Inject
    WebhookPublisher publisher;
    

    @Test
    public void testPublish() throws Exception {
        given()
                .when()
                .log().all()
                .body(new WebhookEvent("test_topic2", new HashMap<>()))
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .post("receive-callback")
                .then()
                .log().all()
                .statusCode(200);
    }

}
