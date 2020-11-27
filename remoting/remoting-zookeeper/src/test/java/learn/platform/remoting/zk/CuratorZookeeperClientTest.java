package learn.platform.remoting.zk;

import learn.platform.commons.url.UrlResource;
import learn.platform.commons.util.NetUtil;
import learn.platform.remoting.zk.support.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.WatchedEvent;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class CuratorZookeeperClientTest {

    private TestingServer zkServer;

    private CuratorZookeeperClient zkClient;
    CuratorFramework server = null;

    private String path = "/dubbo/learn.platform.remoting.zk.CuratorZookeeperClientTest/providers";

    @Before
    public void setup() throws Exception {
        int zkServerPort = NetUtil.getAvailablePort();
        zkServer = new TestingServer(zkServerPort, true);
        zkServer.start();

        zkClient = new CuratorZookeeperClient(UrlResource.valueOf("zookeeper://localhost:" + zkServerPort));
    }

    @Test
    public void testCheckExists() {
        zkClient.create(path, true);
        assertThat(zkClient.checkExists(path), Is.is(true));
        assertThat(zkClient.checkExists(path + "notExisted"), Is.is(false));
    }

    /**
     * 父节点必须是持久节点
     */
    @Test
    public void testChildren() {
        zkClient.create(path, true);
        zkClient.create(path + "/provider1", false);
        zkClient.create(path + "/provider2", false);
        assertThat(zkClient.getChildren(path).size(), Is.is(2));
    }

    @Test
    public void testChildListener() throws InterruptedException {
        zkClient.create(path, true);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        zkClient.addTargetChildrenListener(path, new CuratorZookeeperClient.CuratorWatcherImpl() {
            @Override
            public void process(WatchedEvent event) throws Exception {
                countDownLatch.countDown();
            }
        });
        zkClient.create(path + "/provider1", false);
        countDownLatch.await();
        assertThat(countDownLatch.getCount(), Is.is(0L));
    }

    @Test
    public void testInvalidServer() {
        Assert.assertThrows(IllegalStateException.class,
                () -> zkClient = new CuratorZookeeperClient(UrlResource.valueOf("zookeeper://localhost/service"))
        );
    }

    @Test
    public void testStopServer() {
        Assert.assertThrows(IllegalStateException.class, () -> {
            zkClient.create(path, true);
            zkServer.stop();
            zkClient.delete(path);
        });
    }

    @Test
    public void testChildrenListener() {
        ChildListener childListener = mock(ChildListener.class);
        zkClient.create("/children", false);
        zkClient.addChildListener("/children", childListener);
        zkClient.removeChildListener("/children", childListener);
    }

    @Test
    public void testCreateExistedPath() {
        zkClient.create(path, true);
        zkClient.create(path, true);
    }

    @Test
    public void testConnectState() {
        zkClient.create(path, false);
        assertThat(zkClient.isConnected(), Is.is(true));
    }

    @Test
    public void testCreatePersistent() {
        String content = "mockContentTest";
        zkClient.delete(path);
        assertThat(zkClient.checkExists(path), Is.is(false));
        assertNull(zkClient.getContent(path));

        zkClient.create(path, content, false);
        assertThat(zkClient.checkExists(path), Is.is(true));
        assertEquals(content, zkClient.getContent(path));
    }

    @Test
    public void testCreateEphemeral() {
        String content = "mockContentTest";
        zkClient.delete(path);
        assertThat(zkClient.checkExists(path), Is.is(false));
        assertNull(zkClient.getContent(path));

        zkClient.create(path, content, true);
        assertThat(zkClient.checkExists(path), Is.is(true));
        assertEquals(content, zkClient.getContent(path));
    }

    @After
    public void testClose() throws IOException {
        zkClient.close();
        zkServer.stop();
    }

    @Test
    public void testAddTargetDataListener() throws Exception {
        String listenerPath = path + "/dat/data";
        String value = "vav";

        // 创建一个临时节点
        zkClient.create(listenerPath + "/d.json", value, false);
        assertEquals(value, zkClient.getContent(listenerPath + "/d.json"));

        final AtomicInteger atomicInteger = new AtomicInteger(0);
        zkClient.addTargetDataListener(path, new CuratorZookeeperClient.CuratorWatcherImpl() {
            @Override
            public void childEvent(CuratorFramework zkClient, TreeCacheEvent treeCacheEvent) throws Exception {
                System.out.println("===" + treeCacheEvent);
                atomicInteger.incrementAndGet();
            }
        }, null);

        assertNotNull(zkClient.getContent(listenerPath + "/d.json"));

        zkClient.getClient().setData().forPath(listenerPath + "/d.json", "asdasfa".getBytes());
        zkClient.getClient().setData().forPath(listenerPath + "/d.json", "bvvzxcz".getBytes());

        zkClient.delete(listenerPath + "/d.json");
        zkClient.delete(listenerPath);
        assertNull(zkClient.getContent(listenerPath + "/d.json"));

        System.out.println(atomicInteger.get());
    }
}
