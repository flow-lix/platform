package learn.platform.rpc.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

@Slf4j
public abstract class AbstractZookeeperService {

    private String registryAddress;

    private ZooKeeper zooKeeper;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public AbstractZookeeperService(String registryAddress) {
        this.registryAddress = registryAddress;
        this.zooKeeper = connectServer();
    }

    protected ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZookeeperConstant.SESSION_TIMEOUT, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            log.error("Zookeeper 连接失败!", e);
        }
        return zk;
    }

    protected void stop() {
        if (zooKeeper != null) {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                log.error("关闭Zookeeper客户端失败!", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
}
