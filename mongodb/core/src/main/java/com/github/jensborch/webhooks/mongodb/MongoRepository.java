package com.github.jensborch.webhooks.mongodb;

import static org.bson.codecs.pojo.Conventions.SET_PRIVATE_FIELDS_CONVENTION;

import java.util.Collections;
import java.util.List;

import com.github.jensborch.webhooks.Webhook;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 * Abstract MongoDB repository bass class, responsible for doing POJO codec
 * configuration. This will thus set MongoDB private field convention for the
 * needed class.
 *
 * @param <T> the type handled by the repository.
 */
public abstract class MongoRepository<T> {

    private static final List<Convention> CONVENTIONS = Collections.singletonList(SET_PRIVATE_FIELDS_CONVENTION);
    private static final PojoCodecProvider PROVIDER = PojoCodecProvider
            .builder()
            .register(Webhook.class.getName().substring(0, Webhook.class.getName().lastIndexOf('.')))
            .conventions(CONVENTIONS).build();

    private CodecRegistry registry() {
        return CodecRegistries.fromRegistries(CodecRegistries.fromProviders(PROVIDER), db().getCodecRegistry());
    }

    /**
     * Return a collection configured with the correct POJO codec conventions.
     *
     * @param clazz the collection type
     * @return a MongoDB collection
     */
    protected MongoCollection<T> collection(final Class<T> clazz) {
        return db().withCodecRegistry(registry()).getCollection(collectionName(), clazz);
    }

    /**
     * @return the name of the MongoDB collection.
     */
    protected abstract String collectionName();

    /**
     * This should be overridden to return a MongoDB database configured with
     * the needed codecs for {@link java.net.URI} and
     * {@link java.time.ZonedDateTime} - e.g. {@link URICodec} and
     * {@link ZonedDateTimeCodec}.
     *
     * @return a MongoDB database
     */
    @SuppressWarnings("PMD.ShortMethodName")
    protected abstract MongoDatabase db();

}
