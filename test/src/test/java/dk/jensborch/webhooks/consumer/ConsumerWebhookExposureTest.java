package dk.jensborch.webhooks.consumer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;

import javax.inject.Inject;

import dk.jensborch.webhooks.Webhook;
import dk.jensborch.webhooks.publisher.*;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PublisherWebhookExposure}.
 */
@QuarkusTest
public class ConsumerWebhookExposureTest {

    @Inject
    WebhookRegistry registry;

    private static final String TEST_TOPIC = ConsumerWebhookExposureTest.class.getName();
    private static Webhook webhook;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), TEST_TOPIC);
    }

    @BeforeEach
    public void setUp() {
        registry.register(webhook.state(Webhook.State.REGISTER));
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void testGetWebhook() {
        given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .pathParam("id", webhook.getId())
                .get("consumer-webhooks/{id}")
                .then()
                .statusCode(200)
                .body("topics[0]", is(TEST_TOPIC));
    }

    @Test
    public void testListWebhooksWithTopics() {
        given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .queryParam("topics", TEST_TOPIC + ",testtest")
                .get("consumer-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    public void testListWebhooksUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .queryParam("topics", "unknown")
                .get("consumer-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testListWebhooks() {
        given()
                .spec(spec)
                .auth().basic("consumer", "concon")
                .when()
                .get("consumer-webhooks")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

}
