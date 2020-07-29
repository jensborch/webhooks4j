package com.github.jensborch.webhooks.publisher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;

import com.github.jensborch.webhooks.Webhook;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.eclipse.microprofile.config.ConfigProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Integration test for {@link PublisherWebhookExposure}.
 */
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class PublisherWebhookExposureTest {

    private static final String TEST_TOPIC = PublisherWebhookExposureTest.class.getName();
    private RequestSpecification spec;
    private Webhook webhook;

    @BeforeEach
    void setUp() throws Exception {
        URI uri = new URI("http://localhost:" + ConfigProvider.getConfig().getOptionalValue("quarkus.http.test-port", String.class).orElse("8081"));
        webhook = new Webhook(uri, uri, TEST_TOPIC)
                .state(Webhook.State.SUBSCRIBE);
        spec = new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    @Order(1)
    void testCreateWebhook() {
        String location = given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .body(webhook)
                .post("publisher-webhooks")
                .then()
                .statusCode(201)
                .extract()
                .header("location");
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .get(location)
                .then()
                .statusCode(200)
                .body("topics[0]", is(TEST_TOPIC));
    }

    @Test
    @Order(2)
    void testDeleteWebhook() {
        given()
                .spec(spec)
                .auth().basic("subscriber", "concon")
                .when()
                .pathParam("id", webhook.getId())
                .delete("publisher-webhooks/{id}")
                .then()
                .statusCode(204);
    }

    @Test
    @Order(2)
    void testListWebhooksWithTopics() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("topics", TEST_TOPIC + ",testtest")
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("webhooks.size()", equalTo(1));
    }

    @Test
    @Order(2)
    void testListWebhooksUnknownTopic() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .queryParam("topics", "unknown")
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("webhooks.size()", equalTo(0));
    }

    @Test
    @Order(2)
    void testListWebhooks() {
        given()
                .spec(spec)
                .auth().basic("publisher", "pubpub")
                .when()
                .get("publisher-webhooks")
                .then()
                .statusCode(200)
                .body("webhooks.size()", greaterThan(0));
    }

}
