package dk.jensborch.webhooks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
//@Provider
public class LoggingFilter implements ContainerResponseFilter, ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        if (responseContext.hasEntity()) {
            LOG.info("Response entity: {}", responseContext.getEntity());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOG.info("Method: {}", requestContext.getMethod());
        LOG.info("URI: {}", requestContext.getUriInfo().getRequestUri());
        if (requestContext.hasEntity()) {
            String text = null;
            try (Scanner scanner = new Scanner(requestContext.getEntityStream(), StandardCharsets.UTF_8)) {
                text = scanner.useDelimiter("\\A").next();
            }
            LOG.info("Request entity: {}", text);
            requestContext.setEntityStream(new ByteArrayInputStream(text.getBytes()));
        }
    }


}
