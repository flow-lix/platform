package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.commons.util.NetUtil;
import learn.platform.registry.NotifyListener;
import org.apache.curator.test.TestingServer;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class ZookeeperRegistryTest {

    /**
     * 注册地址
     */
    private UrlResource registryUrl;

    private String service = "org.apache.dubbo.test.injvmServie";

    /**
     * 服务地址
     */
    private UrlResource serverUrl = UrlResource.valueOf("zookeeper://zookeeper" + service);

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

    @After
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

        Map<Resource, Set<NotifyListener>> subscribed = zookeeperRegistry.getSubscribed();
        assertThat(subscribed.size(), Is.is(1));
        assertThat(subscribed.get(serverUrl).size(), Is.is(1));

        zookeeperRegistry.unsubscribe(serverUrl, notifyListener);
        subscribed = zookeeperRegistry.getSubscribed();
        assertThat(subscribed.size(), Is.is(1));
        assertThat(subscribed.get(serverUrl).size(), Is.is(0));
    }

    @Test
    public void testAvailable() {
        zookeeperRegistry.register(serverUrl);
        assertThat(zookeeperRegistry.isAvailable(), Is.is(true));

        zookeeperRegistry.destroy();
        assertThat(zookeeperRegistry.isAvailable(), Is.is(false));
    }

    @Test
    public void testLookup() {
        List<Resource> resourceList = zookeeperRegistry.lookup(serverUrl);
        assertThat(resourceList.size(), Is.is(0));

        zookeeperRegistry.register(serverUrl);
        resourceList = zookeeperRegistry.lookup(serverUrl);
        assertThat(resourceList.size(), Is.is(1));
    }

    @Test
    public void testSubscribeAny() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        zookeeperRegistry.register(serverUrl);
        zookeeperRegistry.subscribe(serverUrl, (a) -> latch.countDown());
        zookeeperRegistry.register(serverUrl);
        latch.await();
    }

}
