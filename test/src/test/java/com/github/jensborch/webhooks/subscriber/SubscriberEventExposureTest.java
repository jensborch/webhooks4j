package com.github.jensborch.webhooks.subscriber;


import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test for
 * {@link com.github.jensborch.webhooks.subscriber.SubscriberEventExposure}
 */
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SubscriberEventExposureTest {

    @Inject
    WebhookSubscriptions subscriptions;

    private static final String TEST_TOPIC = SubscriberEventExposureTest.class.getName();
    private static Webhook webhook;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        URI uri = new URI("http://localhost:" + ConfigProvider.getConfig().getOptionalValue("quarkus.http.test-port", String.class).orElse("8081"));
        webhook = new Webhook(uri, uri, TEST_TOPIC);
    }

    @BeforeEach
    void setUp() {
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    @Order(1)
    void testPublishEvent() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(webhook.getId()))
                .post("subscriber-events")
                .then()
                .statusCode(201);
    }

    @Test
    @Order(2)
    void testList() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .get("subscriber-events")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    void testListUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("from", "2007-12-03T10:15:30+01:00")
                .queryParam("topics", "unknown")
                .get("subscriber-events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void testPublishInvalidEvent() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body("{}")
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.VALIDATION_ERROR.toString()));
    }

    @Test
    void testPublishEventInvalidPublisher() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent(TEST_TOPIC, new HashMap<>()).webhook(UUID.randomUUID()))
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.UNKNOWN_PUBLISHER.toString()));
    }

    @Test
    void testPublishEventUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .body(new WebhookEvent("unknown", new HashMap<>()).webhook(webhook.getId()))
                .post("subscriber-events")
                .then()
                .statusCode(400)
                .body("code", equalTo(WebhookError.Code.UNKNOWN_PUBLISHER.toString()));
    }

}
