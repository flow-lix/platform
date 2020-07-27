package learn.flow.kafka.message;

import learn.flow.kafka.enums.TestEnum;
import lombok.Data;

@Data
public class TestMessage {

    private long timestamp;
    private String mockMsg;
    private TestEnum testEnum;

    public TestMessage(long timestamp, String mockMsg) {
        this.timestamp = timestamp;
        this.mockMsg = mockMsg;
    }
}
