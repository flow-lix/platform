package learn.flow.kafka.serializer;

public interface Serializer<T> {

    byte[] serializer(T obj) throws Exception;

    T deserializer(byte[] bytes) throws Exception;
}
