package learn.flow.kafka.producer;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import learn.flow.kafka.enums.TestEnum;
import learn.flow.kafka.message.TestMessage;
import learn.flow.kafka.serializer.JsonSerializer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.*;

public class LogMessageProducer implements Runnable {

    private String kTopic;
    private int messageNums;
//    private Serializer<TestMessage> serializer;

    private static final Properties PROPERTIES = new Properties();

    static {
        PROPERTIES.setProperty("bootstrap.servers", "localhost:9988");
        PROPERTIES.put("key.serializer", StringSerializer.class);
        PROPERTIES.put("value.serializer", JsonSerializer.class);
    }

    public LogMessageProducer(String kTopic, int messageNums) {
        this.kTopic = kTopic;
        this.messageNums = messageNums;
    }

    @Override
    public void run() {
        mockGenerateData();
    }

    public void scheduleAtFix() {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat("kafka-produce-pool-%d")
                .build();

        ScheduledExecutorService scheduledService = new ScheduledThreadPoolExecutor(3, threadFactory, new ThreadPoolExecutor.AbortPolicy());
        while (!scheduledService.isShutdown()) {
            scheduledService.scheduleAtFixedRate(this, 0, 10000, TimeUnit.MILLISECONDS);
        }
    }

    private void mockGenerateData() {
        Producer<String, TestMessage> producer = new KafkaProducer<>(PROPERTIES);
        for (int i = 0; i < messageNums; i++) {
            TestMessage message = new TestMessage(System.currentTimeMillis(), "some_value_" + i);
            if (i % 2 == 0) {
                message.setTestEnum(TestEnum.SOME_VALUE);
            } else {
                message.setTestEnum(TestEnum.OTHER_VALUE);
            }
            try {
                ProducerRecord<String, TestMessage> record = new ProducerRecord<>(kTopic, String.valueOf(i), message);
                producer.send(record);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}
