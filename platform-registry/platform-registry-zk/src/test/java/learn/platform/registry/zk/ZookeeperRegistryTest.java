package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.commons.util.NetUtil;
import learn.platform.registry.NotifyListener;
import org.apache.curator.test.TestingServer;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class ZookeeperRegistryTest {

    /**
     * 注册地址
     */
    private UrlResource registryUrl;
    /**
     * 服务地址
     */
    private UrlResource serverUrl = UrlResource.valueOf("zookeeper://zookeeper");
    /**
     * Mock Zookeeper服务器
     */
    private TestingServer zkServer;

    private ZookeeperRegistry zookeeperRegistry;

    @Before
    public void setUp() throws Exception {
        int zkServerPort = NetUtil.getAvailablePort();
        zkServer = new TestingServer(zkServerPort, true);
        zkServer.start();

        registryUrl = UrlResource.valueOf("zookeeper://localhost:" + zkServerPort);
        ZookeeperRegistryFactory registryFactory = new ZookeeperRegistryFactory();
        zookeeperRegistry = (ZookeeperRegistry) registryFactory.createRegistry(registryUrl);
    }

    @Test
    public void destroy() throws Exception{
        zkServer.stop();
    }

    @Test
    public void testAnyHost() {
        assertThrows(IllegalStateException.class, () ->
                new ZookeeperRegistryFactory().createRegistry(UrlResource.valueOf("multicast://0.0.0.0/"))
        );
    }

    @Test
    public void testRegister() {
        Set<Resource> registered;
        for (int i = 0; i < 2; i++) {
            zookeeperRegistry.register(serverUrl);
            registered = zookeeperRegistry.getRegistered();
            assertThat(registered.contains(serverUrl), Is.is(true));
        }

        registered = zookeeperRegistry.getRegistered();
        assertThat(registered.size(), Is.is(1));
    }

    @Test
    public void testSubscribe() {
        NotifyListener notifyListener = mock(NotifyListener.class);
        zookeeperRegistry.subscribe(serverUrl, notifyListener);

    }
}
