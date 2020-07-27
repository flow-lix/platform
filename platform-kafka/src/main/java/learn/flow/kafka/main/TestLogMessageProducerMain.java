package learn.flow.kafka.main;

import learn.flow.kafka.producer.LogMessageProducer;

public class TestLogMessageProducerMain {

    public static void main(String[] args) {
        new LogMessageProducer("topicOne", 100).scheduleAtFix();
    }
}
