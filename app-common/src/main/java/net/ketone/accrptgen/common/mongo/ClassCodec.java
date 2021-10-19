package net.ketone.accrptgen.common.mongo;

import lombok.extern.slf4j.Slf4j;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

@Slf4j
public class ClassCodec implements Codec<Class> {
    @Override
    public Class decode(BsonReader bsonReader, DecoderContext decoderContext) {
        try {
            return Class.forName(bsonReader.readString());
        } catch (ClassNotFoundException e) {
            log.error("Error decoding Class", e);
        }
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, Class aClass, EncoderContext encoderContext) {
        bsonWriter.writeString(aClass.getCanonicalName());
    }

    @Override
    public Class<Class> getEncoderClass() {
        return Class.class;
    }
}
