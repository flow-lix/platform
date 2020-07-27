package learn.platform.rpc.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import static learn.platform.rpc.zookeeper.ZookeeperConstant.ZK_DATA_PATH;
import static learn.platform.rpc.zookeeper.ZookeeperConstant.ZK_REGISTRY_PATH;

@Slf4j
public class ServiceRegistry extends AbstractZookeeperService {

    public ServiceRegistry(String registryAddress) {
        super(registryAddress);
    }

    public void register(String data) {
        if (StringUtils.isNotEmpty(data)) {
            ZooKeeper zooKeeper = connectServer();
            if (zooKeeper != null) {
                addRootNode(zooKeeper);
                createNode(zooKeeper, data);
            }
        }
    }

    private void createNode(ZooKeeper zooKeeper, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zooKeeper.create(ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.debug("创建Zookeeper节点 {} -> {}", path, data);
        } catch (Exception e) {
            log.error("添加临时节点失败!", e);
        }
    }

    private void addRootNode(ZooKeeper zooKeeper) {
        try {
            Stat stat = zooKeeper.exists(ZK_REGISTRY_PATH, false);
            if (stat == null) {
                zooKeeper.create(ZK_REGISTRY_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (Exception e) {
            log.error("添加根节点失败!", e);
        }
    }

}
