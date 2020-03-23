package com.github.jensborch.webhooks.subscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jensborch.webhooks.Webhook;
import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventData;

/**
 *
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
public class WebhookEventMessageBodyReader implements MessageBodyReader<WebhookEvent> {

    @Inject
    WebhookSubscriptions subscriptions;

    @Context
    Providers workers;

    @Override
    public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
        return type == WebhookEvent.class;
    }

    @Override
    public WebhookEvent readFrom(final Class<WebhookEvent> type, final Type genericType, final Annotation[] annotations,
            final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
            throws IOException {

        ContextResolver<ObjectMapper> objectMapperResolver = workers.getContextResolver(ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE);

        String data = readEntity(entityStream);
        ObjectMapper mapper = objectMapperResolver.getContext(ObjectMapper.class);
        WebhookEvent event = mapper.readValue(data, WebhookEvent.class);
        Class<?> clazz = subscriptions.find(event).map(Webhook::getType).orElse(WebhookEventData.class);
        return mapper.readValue(data, new WebhookTypeReference(clazz));
    }

    private String readEntity(final InputStream entityStream) throws IOException {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(entityStream, StandardCharsets.UTF_8))) {
            return br.lines().collect(Collectors.joining());
        }
    }

    private static class WebhookTypeReference extends TypeReference<WebhookEvent<?>> {

        private final Class<?> clazz;

        public WebhookTypeReference(final Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Type getType() {
            return new ParameterizedType() {
                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{null == clazz ? WebhookEventData.class : clazz};
                }

                @Override
                public Type getRawType() {
                    return Webhook.class;
                }

                @Override
                public Type getOwnerType() {
                    return null;
                }

            };
        }

        @Override
        public int compareTo(TypeReference<WebhookEvent<?>> o) {
            return getType().toString().compareTo(o.getType().toString());
        }

    }

}
