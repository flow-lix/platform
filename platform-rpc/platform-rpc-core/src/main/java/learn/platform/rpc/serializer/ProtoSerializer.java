package learn.platform.rpc.serializer;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ProtoSerializer<T> {

    private static ConcurrentMap<Class<?>, Schema<?>> schemaConcurrentMap = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<?> clz) {
        return (Schema<T>)schemaConcurrentMap.computeIfAbsent(clz, a -> RuntimeSchema.createFrom(clz));
    }

    public static <T> byte[] serializer(T message) {
        Schema<T> schema = getSchema(message.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtobufIOUtil.toByteArray(message, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    public static <T> T deserializer(byte[] content, Class<?> clz) throws Exception {
        @SuppressWarnings("unchecked")
        T instance = (T) clz.newInstance();
        Schema<T> schema = getSchema(clz);
        ProtobufIOUtil.mergeFrom(content, instance, schema);
        return instance;
    }
}
