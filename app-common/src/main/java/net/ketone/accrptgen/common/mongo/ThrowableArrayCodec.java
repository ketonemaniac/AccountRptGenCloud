package net.ketone.accrptgen.common.mongo;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class ThrowableArrayCodec implements Codec<Throwable[]> {
    @Override
    public Throwable[] decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readString();
        return new Throwable[0];
    }

    @Override
    public void encode(BsonWriter bsonWriter, Throwable[] aClass, EncoderContext encoderContext) {
        bsonWriter.writeString(aClass.toString());
    }

    @Override
    public Class<Throwable[]> getEncoderClass() {
        return Throwable[].class;
    }
}
