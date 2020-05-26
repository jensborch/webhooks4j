package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TreeSet;

import com.github.jensborch.webhooks.WebhookEventStatus;
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
 * Test for {@link AbstractStatusRepository}.
 */
@ExtendWith(MockitoExtension.class)
public class AbstractStatusRepositoryTest {

    private static final ZonedDateTime DATE_TIME = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.of("Europe/Copenhagen"));

    @Mock
    private MongoCollection<WebhookEventStatus> collection;

    @Mock
    private MongoDatabase db;

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
    public void setup() {
        when(db.withCodecRegistry(any())).thenReturn(db);
        when(db.getCollection(any(String.class), any(Class.class))).thenReturn(collection);
        FindIterable<?> iterable = mock(FindIterable.class);
        when(iterable.into(any())).thenReturn(new TreeSet<>());
        doReturn(iterable).when(collection).find(any(Bson.class));
    }

    @Test
    public void testListWithTopics() {
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
    public void testListWithoutTopics() {
        repository.list(DATE_TIME);

        verify(collection, times(1)).find(captor.capture());
        assertEquals(
                "Operator Filter{fieldName='start', operator='$gt', value=2020-01-01T00:00+01:00[Europe/Copenhagen]}",
                captor.getValue().toString()
        );
    }

}
