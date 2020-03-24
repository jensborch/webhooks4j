package com.github.jensborch.webhooks.subscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NoContentException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookError;
import com.github.jensborch.webhooks.WebhookEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MessageBodyReader for reading events and converting them to the right generic
 * {@link WebhookEvent}.
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class WebhookEventMessageBodyReader implements MessageBodyReader<WebhookEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookEventMessageBodyReader.class);

    @Inject
    WebhookSubscriptions subscriptions;

    @Context
    Providers workers;

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return type == WebhookEvent.class && mediaType == MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    @SuppressWarnings("PMD.PreserveStackTrace")
    public WebhookEvent readFrom(final Class<WebhookEvent> type, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
            throws IOException {
        String data = readEntity(entityStream);
        if (data == null || data.isEmpty()) {
            LOG.debug("No webhook event data");
            throw new NoContentException("No webhook event data");
        } else {
            try {
                ContextResolver<ObjectMapper> objectMapperResolver = workers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);
                ObjectMapper mapper = objectMapperResolver.getContext(ObjectMapper.class);
                WebhookEvent event = mapper.readValue(data, WebhookEvent.class);
                Class<?> clazz = subscriptions.find(event).map(Webhook::getType).orElseThrow(()
                        -> webApplicationException(WebhookError.Code.NOT_FOUND, "Could not find webhook " + event.getWebhook())
                );
                return mapper.readValue(data, typeReference(clazz));
            } catch (JsonProcessingException e) {
                LOG.debug("Json processing exception reading event data", e);
                throw webApplicationException(WebhookError.Code.VALIDATION_ERROR, e.getMessage());
            }
        }
    }

    private WebApplicationException webApplicationException(final WebhookError.Code code, final String msg) {
        WebhookError error = new WebhookError(code, msg);
        return new WebApplicationException(Response.status(code.getStatus())
                .entity(error)
                .build());
    }

    private String readEntity(final InputStream entityStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(entityStream, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
        }
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    private <T> TypeReference<WebhookEvent<T>> typeReference(final Class<T> type) {
        return new TypeReference<WebhookEvent<T>>() {
        };
    }

}
