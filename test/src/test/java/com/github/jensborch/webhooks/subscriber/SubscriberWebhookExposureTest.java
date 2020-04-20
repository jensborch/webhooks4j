package com.github.jensborch.webhooks.subscriber;


import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;

import javax.inject.Inject;

import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.publisher.PublisherWebhookExposure;
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
public class SubscriberWebhookExposureTest {

    @Inject
    WebhookSubscriptions subscriptions;

    private static final String TEST_TOPIC = SubscriberWebhookExposureTest.class.getName();
    private static Webhook webhook;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), TEST_TOPIC);
    }

    @BeforeEach
    public void setUp() {
        subscriptions.subscribe(webhook.state(Webhook.State.SUBSCRIBE));
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
                .auth().basic("subscriber", "concon")
                .when()
                .pathParam("id", webhook.getId())
                .get("subscriber-webhooks/{id}")
                .then()
                .statusCode(200)
                .body("topics[0]", is(TEST_TOPIC));
    }

    @Test
    public void testListWebhooksWithTopics() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("topics", TEST_TOPIC + ",testtest")
                .get("subscriber-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }

    @Test
    public void testListWebhooksUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .queryParam("topics", "unknown")
                .get("subscriber-webhooks")
                .then()
                .statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    public void testListWebhooks() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .get("subscriber-webhooks")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test
    public void testDeleteWebhooks() throws Exception {
        Webhook toDelete = new Webhook(new URI("http://localhost:8081/"), new URI("http://localhost:8081/"), "delete");
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .body(toDelete.state(Webhook.State.SUBSCRIBE))
                .post("subscriber-webhooks")
                .then()
                .statusCode(201);
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .pathParam("id", toDelete.getId())
                .delete("subscriber-webhooks/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    public void testUpdateWebhook400() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .body(webhook.state(Webhook.State.UNSUBSCRIBE))
                .pathParam("id", webhook.getId())
                .put("subscriber-webhooks/{id}")
                .then()
                .statusCode(400);
    }

    @Test
    public void testUpdateWebhookSync() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .body(webhook.state(Webhook.State.SYNCHRONIZE))
                .pathParam("id", webhook.getId())
                .put("subscriber-webhooks/{id}")
                .then()
                .statusCode(200);
    }

}
