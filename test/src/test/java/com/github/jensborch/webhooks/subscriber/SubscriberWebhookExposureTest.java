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
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PublisherWebhookExposure}.
 */
@QuarkusTest
class SubscriberWebhookExposureTest {

    @Inject
    WebhookSubscriptions subscriptions;

    private static final String TEST_TOPIC = SubscriberWebhookExposureTest.class.getName();
    private static Webhook webhook;
    private static URI uri;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        uri = new URI("http://localhost:" + ConfigProvider.getConfig().getOptionalValue("quarkus.http.test-port", String.class).orElse("8081"));
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
    void testGetWebhook() {
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
    void testListWebhooksWithTopics() {
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
    void testListWebhooksUnknownTopic() {
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
    void testListWebhooks() {
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
    void testDeleteWebhooks() throws Exception {
        Webhook toDelete = new Webhook(uri, uri, "delete");
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
    void testUpdateWebhook400() {
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
    void testUpdateWebhookSync() {
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
