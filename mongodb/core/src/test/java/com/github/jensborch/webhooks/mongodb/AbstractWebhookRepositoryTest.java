package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import com.github.jensborch.webhooks.Webhook;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link AbstractWebhookRepository}.
 */
@ExtendWith(MockitoExtension.class)
class AbstractWebhookRepositoryTest {

    @Mock
    private MongoCollection<Webhook> collection;

    @Mock
    private MongoDatabase db;

    @Mock
    private FindIterable iterable;

    private final AbstractWebhookRepository repository = new AbstractWebhookRepository() {
        @Override
        protected String collectionName() {
            return "test";
        }

        @Override
        protected MongoDatabase db() {
            return db;
        }

        @Override
        protected AbstractStatusRepository statusRepository() {
            return mock(AbstractStatusRepository.class);
        }

    };

    private final ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);

    @BeforeEach
    void setup() {
        when(db.withCodecRegistry(any())).thenReturn(db);
        when(db.getCollection(any(String.class), any(Class.class))).thenReturn(collection);
        lenient().when(iterable.into(any())).thenReturn(new HashSet<>());
        lenient().when(iterable.limit(eq(1))).thenReturn(iterable);
        lenient().doReturn(iterable).when(collection).find(any(Bson.class));

    }

    @Test
    void testListWithTopics() {
        repository.list("a", "b", "c");

        verify(collection, times(1)).find(captor.capture());
        assertEquals("{\"topics\": {\"$elemMatch\": {\"$in\": [\"a\", \"b\", \"c\"]}}}", captor.getValue().toString());
    }

    @Test
    void testListWithoutTopics() {
        repository.list();
        verify(collection, times(1)).find(captor.capture());
        assertEquals("{}", captor.getValue().toString());
    }

    @Test
    void testSave() throws Exception {
        Webhook webhook = new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), "topics");
        repository.save(webhook);
        verify(collection, times(1)).replaceOne(any(Bson.class), eq(webhook), any(ReplaceOptions.class));
    }

    @Test
    void testDelete() throws Exception {
        UUID id = UUID.randomUUID();
        repository.delete(id);
        verify(collection, times(1)).deleteOne(eq(Filters.eq("_id", id)));
    }

    @Test
    void testFind() throws Exception {
        Webhook webhook = new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), "topics");
        when(iterable.first()).thenReturn(webhook);
        UUID id = UUID.randomUUID();
        Optional<Webhook> found = repository.find(id);
        assertEquals(webhook, found.get());
    }

    @Test
    void testTouchNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        repository.touch(id);
        verify(collection, times(0)).replaceOne(any(Bson.class), any(Webhook.class), any(ReplaceOptions.class));
    }

    @Test
    void testTouch() throws Exception {
        Webhook webhook = new Webhook(new URI("http://test.dk"), new URI("http://test.dk"), "topics");
        when(iterable.first()).thenReturn(webhook);
        repository.touch(webhook.getId());
        verify(collection, times(1)).replaceOne(any(Bson.class), any(Webhook.class));
    }

}
