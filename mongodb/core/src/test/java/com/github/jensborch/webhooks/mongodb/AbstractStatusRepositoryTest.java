package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;

import com.github.jensborch.webhooks.WebhookEvent;
import com.github.jensborch.webhooks.WebhookEventStatus;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link AbstractStatusRepository}.
 */
@ExtendWith(MockitoExtension.class)
class AbstractStatusRepositoryTest {

    private static final ZonedDateTime DATE_TIME = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Copenhagen"));

    @Mock
    private MongoCollection<WebhookEventStatus> collection;

    @Mock
    private MongoDatabase db;

    @Mock
    private FindIterable iterable;

    private final AbstractStatusRepository repository = new AbstractStatusRepository() {
        @Override
        protected String collectionName() {
            return "test";
        }

        @Override
        protected MongoDatabase db() {
            return db;
        }

    };

    private final ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);

    @BeforeEach
    void setup() {
        when(db.withCodecRegistry(any())).thenReturn(db);
        when(db.getCollection(any(String.class), any(Class.class))).thenReturn(collection);
        lenient().when(iterable.into(any())).thenReturn(new TreeSet<>());
        lenient().when(iterable.limit(eq(1))).thenReturn(iterable);
        lenient().doReturn(iterable).when(collection).find(any(Bson.class));
    }

    @Test
    void testListWithTopics() {
        repository.list(DATE_TIME, "a", "b", "c");

        verify(collection, times(1)).find(captor.capture());
        assertEquals(
                "And Filter{filters=["
                + "Operator Filter{fieldName='start', operator='$gt', value=2020-01-01T00:00+01:00[Europe/Copenhagen]}, "
                + "Operator Filter{fieldName='event.topic', operator='$in', value=[a, b, c]}"
                + "]}",
                captor.getValue().toString()
        );
    }

    @Test
    void testListWithoutTopics() {
        repository.list(DATE_TIME);

        verify(collection, times(1)).find(captor.capture());
        assertEquals(
                "Operator Filter{fieldName='start', operator='$gt', value=2020-01-01T00:00+01:00[Europe/Copenhagen]}",
                captor.getValue().toString()
        );
    }

    @Test
    void testSave() throws Exception {
        WebhookEvent event = new WebhookEvent("topic", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event);
        repository.save(status);
        verify(collection, times(1)).replaceOne(any(Bson.class), eq(status), any(ReplaceOptions.class));
    }

    @Test
    void testFind() throws Exception {
        WebhookEvent event = new WebhookEvent("topic", new HashMap<>());
        WebhookEventStatus status = new WebhookEventStatus(event);
        when(iterable.first()).thenReturn(status);
        UUID id = UUID.randomUUID();
        Optional<WebhookEventStatus> found = repository.find(id);
        assertEquals(status, found.get());
    }
}
