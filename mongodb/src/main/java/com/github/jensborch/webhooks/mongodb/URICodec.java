package com.github.jensborch.webhooks.mongodb;

import java.net.URI;
import java.util.Objects;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Codec for mapping URI to BSON.
 */
public class URICodec implements Codec<URI> {

    @Override
    public URI decode(final BsonReader reader, final DecoderContext context) {
        Objects.requireNonNull(reader, "Reader is null");
        return URI.create(reader.readString());
    }

    @Override
    public void encode(final BsonWriter writer, final URI value, final EncoderContext context) {
        Objects.requireNonNull(writer, "Writer is null");
        Objects.requireNonNull(value, "Value is null");
        writer.writeString(value.toString());
    }

    @Override
    public Class<URI> getEncoderClass() {
        return URI.class;
    }
}
