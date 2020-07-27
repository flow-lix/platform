package learn.flow.common;

import learn.flow.commons.connector.ZookeeperConnector;
import learn.flow.config.ZookeeperConfig;
import org.junit.Assert;
import org.junit.Test;

public class ZookeeperConnectorTest {

    public void setUp() {

    }

    @Test
    public void testGetCommittedOffset() {
        verify( "/chroot/consumers/kafka-group/offsets");
    }

    private void verify(String expectedPath) {
        ZookeeperConnector connector = new ZookeeperConnector(new ZookeeperConfig());

        Assert.assertEquals(expectedPath, connector.getCommittedOffsetGroupPath());
    }
}
