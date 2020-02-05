package dk.jensborch.webhooks.publisher;

import static io.restassured.RestAssured.given;

import java.net.URI;

import dk.jensborch.webhooks.Webhook;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link PublisherWebhookExposure}.
 */
@QuarkusTest
public class PublisherWebhookExposureIT {

    private RequestSpecification spec;

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
    public void testCreateWebhook() throws Exception {
        String location = given(spec)
                .when()
                .body(new Webhook(new URI("http://localhost:8081/publisher-webhooks"), new URI("http://localhost:8081/consumer-events"), this.getClass().getName()))
                .post("publisher-webhooks")
                .then()
                .statusCode(201)
                .extract()
                .header("location");

        given(spec)
                .when()
                .get(location)
                .then()
                .statusCode(200);
    }

}
