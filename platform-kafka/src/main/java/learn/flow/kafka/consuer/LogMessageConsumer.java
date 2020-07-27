package learn.flow.kafka.consuer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogMessageConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogMessageConsumer.class);

    private static final String brokerList = "localhost:9092";
    private static final String topic = "topicOne";
    private static final String groupId = "default0";

    private static final AtomicBoolean running = new AtomicBoolean(true);

    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.put("bootstrap.servers", brokerList);
        PROPERTIES.put("key.deserializer", StringDeserializer.class);
        PROPERTIES.put("value.deserializer", BytesDeserializer.class);
        PROPERTIES.put("group.id", groupId);
    }

    public static void main(String[] args) {
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(PROPERTIES)) {
            consumer.subscribe(Collections.singletonList(topic));
            while (running.get()) {
                ConsumerRecords<String, byte[]> record = consumer.poll(Duration.ofMillis(1000));
                record.forEach(r -> {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("Topic: {}, Partition: {}, Offset: {}, key: {}, value: {}",
                                r.topic(), r.partition(), r.offset(), r.key(), r.value());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
