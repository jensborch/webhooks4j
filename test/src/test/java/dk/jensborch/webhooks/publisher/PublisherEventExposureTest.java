package dk.jensborch.webhooks.publisher;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;

import java.net.URI;

import dk.jensborch.webhooks.*;
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
 * {@link dk.jensborch.webhooks.consumer.PublisherEventExposur}
 */
@QuarkusTest
public class PublisherEventExposureTest {

    private static final String TEST_TOPIC = PublisherEventExposureTest.class.getName();
    private static Webhook webhook;
    private RequestSpecification spec;

    @BeforeAll
    public static void setUpClass() throws Exception {
        webhook = new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), TEST_TOPIC);
    }

    @BeforeEach
    public void setUp() throws Exception {
        RequestSpecBuilder builder = new RequestSpecBuilder();
        spec = builder
                .setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .addFilter(new ResponseLoggingFilter())
                .addFilter(new RequestLoggingFilter())
                .build();
    }

    @Test
    public void testRegisterWebhook() {
        String location = given()
                .spec(spec)
                .when()
                .body(webhook)
                .post("publisher-webhooks")
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given()
                .spec(spec)
                .when()
                .get(location)
                .then()
                .statusCode(200)
                .body("size()", greaterThan(1));
    }

}
