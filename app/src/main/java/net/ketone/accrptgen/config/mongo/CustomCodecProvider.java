package net.ketone.accrptgen.config.mongo;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class CustomCodecProvider implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> aClass, CodecRegistry codecRegistry) {
        if(aClass == StackTraceElement[].class) {
            return (Codec<T>) new StackTraceElementArrayCodec();
        } else if(aClass == Throwable[].class) {
            return (Codec<T>) new ThrowableArrayCodec();
        } else if(aClass == StackTraceElement.class) {
            return (Codec<T>) new StackTraceElementCodec();
        }
        return null;
    }
}
