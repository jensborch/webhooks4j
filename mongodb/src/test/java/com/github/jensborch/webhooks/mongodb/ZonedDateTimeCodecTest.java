package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for {@link ZonedDateTimeCodec}.
 */
@ExtendWith(MockitoExtension.class)
class ZonedDateTimeCodecTest {

    private final static ZonedDateTime DATE_TIME = ZonedDateTime.now();

    @Mock
    private BsonReader reader;

    @Mock
    private BsonWriter writer;

    @Test
    public void testDecode() {
        when(reader.readDateTime()).thenReturn(DATE_TIME.toInstant().toEpochMilli());
        assertEquals(DATE_TIME.toInstant().toEpochMilli(), new ZonedDateTimeCodec().decode(reader, null).toInstant().toEpochMilli());
    }

    @Test
    public void testEncode() {
        new ZonedDateTimeCodec().encode(writer, DATE_TIME, null);
        verify(writer).writeDateTime(DATE_TIME.toInstant().toEpochMilli());
    }

    @Test
    public void testGetEncoderClass() {
        assertEquals(ZonedDateTime.class, new ZonedDateTimeCodec().getEncoderClass());
    }

}