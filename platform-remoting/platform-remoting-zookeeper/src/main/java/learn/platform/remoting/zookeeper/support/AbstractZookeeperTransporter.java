package learn.platform.remoting.zookeeper.support;

import learn.platform.commons.Resource;
import learn.platform.remoting.zookeeper.ZookeeperClient;
import learn.platform.remoting.zookeeper.ZookeeperTransporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractZookeeperTransporter implements ZookeeperTransporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractZookeeperTransporter.class);

    /**
     * 集群地址共享一个客户端
     */
    private final ConcurrentMap<String, ZookeeperClient> zookeeperClientMap = new ConcurrentHashMap<>();

    @Override
    public ZookeeperClient connect(Resource resource) {
        String identifyId = resource.getIdentifyId();
        ZookeeperClient zookeeperClient = fetchZookeeperClientCache(identifyId);
        if (zookeeperClient != null) {
            LOGGER.debug("Find valid zookeeper client from cache for resource: {}", identifyId);
            return zookeeperClient;
        }
        synchronized (zookeeperClientMap) {
            zookeeperClient = fetchZookeeperClientCache(identifyId);
            if (zookeeperClient == null){
                zookeeperClient = createZookeeperClient(resource);
                zookeeperClientMap.put(identifyId, zookeeperClient);
            }
        }
        return zookeeperClient;
    }

    protected abstract ZookeeperClient createZookeeperClient(Resource resource);

    /**
     * 获取客户端连接
     * @param identifyId
     * @return
     */
    private ZookeeperClient fetchZookeeperClientCache(String identifyId) {
        ZookeeperClient client = zookeeperClientMap.get(identifyId);
        return client.isConnected() ? client : null;
    }

    private void writeToClientMap(List<String> addressList, ZookeeperClient client) {
        for (String address : addressList) {
            zookeeperClientMap.put(address, client);
        }
    }
}
