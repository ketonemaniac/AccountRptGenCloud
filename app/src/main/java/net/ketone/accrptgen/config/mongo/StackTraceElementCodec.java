package net.ketone.accrptgen.config.mongo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

@Slf4j
public class StackTraceElementCodec implements Codec<StackTraceElement> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public StackTraceElement decode(BsonReader bsonReader, DecoderContext decoderContext) {
        try {
            bsonReader.readStartDocument();
            StackTraceElement ste = mapper.readValue(bsonReader.readString("ste"), StackTraceElement.class);
            return ste;
        } catch (JsonProcessingException e) {
            log.error("Error decoding StackTraceElement", e);
        } finally {
            bsonReader.readEndDocument();
        }
        return null;
    }

    @Override
    public void encode(BsonWriter bsonWriter, StackTraceElement aClass, EncoderContext encoderContext) {
        try {
            bsonWriter.writeStartDocument();
            bsonWriter.writeString("ste", mapper.writeValueAsString(aClass));
            bsonWriter.writeEndDocument();
        } catch (JsonProcessingException e) {
            log.error("Error encoding StackTraceElement", e);
        }
    }

    @Override
    public Class<StackTraceElement> getEncoderClass() {
        return StackTraceElement.class;
    }
}
