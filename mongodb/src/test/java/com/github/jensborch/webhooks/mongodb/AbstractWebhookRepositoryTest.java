package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import com.github.jensborch.webhooks.Webhook;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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
public class AbstractWebhookRepositoryTest {

    @Mock
    private MongoCollection<Webhook> collection;

    private final AbstractWebhookRepository repository = new AbstractWebhookRepository() {
        @Override
        protected MongoCollection<Webhook> collection() {
            return collection;
        }
    };

    private final ArgumentCaptor<Bson> captor = ArgumentCaptor.forClass(Bson.class);

    @BeforeEach
    public void setup() {
        FindIterable iterable = mock(FindIterable.class);
        when(iterable.into(any(HashSet.class))).thenReturn(new HashSet());
        when(collection.find(any(Bson.class))).thenReturn(iterable);
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
