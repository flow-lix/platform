package learn.platform.rpc.zookeeper;

import learn.platform.rpc.connection.ConnectionManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ServiceDiscovery extends AbstractZookeeperService {

    private List<String> data;

    public ServiceDiscovery(String registryAddress) {
        super(registryAddress);
        ZooKeeper zooKeeper = connectServer();
        if (zooKeeper != null) {
            watchNode(zooKeeper);
        }
    }

    private void watchNode(final ZooKeeper zooKeeper) {
        try {
            List<String> nodeList = zooKeeper.getChildren(ZookeeperConstant.ZK_REGISTRY_PATH, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zooKeeper);
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zooKeeper.getData(ZookeeperConstant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            log.debug("node data: {}", dataList);
            this.data = dataList;

            ConnectionManager.getInstance().updateConnectedServer(this.data);
        } catch (Exception e) {
            log.error("获取节点信息失败!", e);
        }
    }

}
