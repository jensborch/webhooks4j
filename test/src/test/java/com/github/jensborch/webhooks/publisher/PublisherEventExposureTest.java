package com.github.jensborch.webhooks.publisher;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.subscriber.WebhookSubscriptions;
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
 * Integration test for
 * {@link com.github.jensborch.webhooks.publisher.PublisherEventExposure}
 */
@QuarkusTest
public class PublisherEventExposureTest {

    @Inject
    WebhookSubscriptions subscriptions;

    @Inject
    WebhookPublisher publisher;

    private static final String TEST_TOPIC = PublisherEventExposureTest.class.getName();
    private static Webhook webhook;
    private static WebhookEvent event;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TEST_TOPIC);
        event = new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(webhook.getId());
    }

    @BeforeEach
    public void setUp() {
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        publisher.publish(event);
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void testGet() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .pathParam("id", event.getId())
                .get("publisher-events/{id}")
                .then()
                .statusCode(200);
    }

    @Test
    public void testListTopics() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", TEST_TOPIC)
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testListWebhook() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("webhook", UUID.randomUUID())
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testListWrongWebhook() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("webhook", 42)
                .get("publisher-events")
                .then()
                .statusCode(400)
                .body("code", equalTo("VALIDATION_ERROR"));
    }

    @Test
    public void testList() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testListUnknownTopics() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", "t1,t2,t3")
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testListFuture() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("from", ZonedDateTime.now().plusHours(1).format(DateTimeFormatter.ISO_DATE_TIME))
                .get("publisher-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

}
