package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.HashSet;

import com.github.jensborch.webhooks.Webhook;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
            throw new UnsupportedOperationException("Not needed yet.");
        }

    };

    private final ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);

    @BeforeEach
    public void setup() {
        when(db.withCodecRegistry(any())).thenReturn(db);
        when(db.getCollection(any(String.class), any(Class.class))).thenReturn(collection);
        FindIterable<?> iterable = mock(FindIterable.class);
        when(iterable.into(any())).thenReturn(new HashSet<>());
        doReturn(iterable).when(collection).find(any(Bson.class));
    }

    @Test
    public void testListWithTopics() {
        repository.list("a", "b", "c");

        verify(collection, times(1)).find(captor.capture());
        assertEquals("{\"topics\": {\"$elemMatch\": {\"$in\": [\"a\", \"b\", \"c\"]}}}", captor.getValue().toString());
    }

    @Test
    public void testListWithoutTopics() {
        repository.list();
        verify(collection, times(1)).find(captor.capture());
        assertEquals("{}", captor.getValue().toString());
    }

}
