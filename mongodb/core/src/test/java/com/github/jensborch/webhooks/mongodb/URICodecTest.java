package com.github.jensborch.webhooks.mongodb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test for MongoDB URI codec.
 */
@ExtendWith(MockitoExtension.class)
class URICodecTest {

    @Mock
    private BsonReader reader;

    @Mock
    private BsonWriter writer;

    @Test
    public void decode() {
        when(reader.readString()).thenReturn("http://test.dk");
        assertEquals("test.dk",new URICodec().decode(reader, null).getHost());
    }

    @Test
    public void encode() throws Exception {
        new URICodec().encode(writer, new URI("http://test.dk"), null);
        verify(writer).writeString("http://test.dk");
    }

    @Test
    public void getEncoderClass() {
        assertEquals(URI.class, new URICodec().getEncoderClass());
    }

}