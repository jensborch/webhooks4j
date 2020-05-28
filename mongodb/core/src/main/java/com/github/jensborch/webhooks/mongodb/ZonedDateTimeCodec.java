package com.github.jensborch.webhooks.mongodb;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Codec for mapping ZonedDateTime to BSON. It stores the value as BSON DateTime
 * values in UTC thus losing nanoseconds and time zone information.
 */
public class ZonedDateTimeCodec implements Codec<ZonedDateTime> {

    @Override
    public ZonedDateTime decode(final BsonReader reader, final DecoderContext context) {
        Objects.requireNonNull(reader, "Reader is null");
        return Instant.ofEpochMilli(reader.readDateTime()).atZone(ZoneOffset.UTC);
    }

    @Override
    public void encode(final BsonWriter writer, final ZonedDateTime value, final EncoderContext context) {
        Objects.requireNonNull(writer, "Writer is null");
        Objects.requireNonNull(value, "Value is null");
        writer.writeDateTime(value.withZoneSameInstant(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    @Override
    public Class<ZonedDateTime> getEncoderClass() {
        return ZonedDateTime.class;
    }
}
