package learn.platform.registry.zk;

import learn.platform.commons.util.NetUtil;
import learn.platform.registry.RegistryService;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static learn.platform.registry.zk.ZookeeperConfig.ROOT_PATH;
import static learn.platform.registry.zk.ZookeeperConfig.ROOT_PATH_WITHOUT_SUFFIX;
import static learn.platform.registry.zk.ZookeeperConfig.ZK_PATH_SPLIT_CHAR;

/**
 * Zookeeper 注册服务
 */
public class ZookeeperRegistryService implements RegistryService<IZkChildListener> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private static volatile ZookeeperRegistryService instance;
    private static volatile ZkClient zkClient;

    private static final ConcurrentMap<String, List<InetSocketAddress>> CLUSTER_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, List<IZkChildListener>> LISTENER_SERVICE_MAP = new ConcurrentHashMap<>();

    private static final int REGISTERED_PATH_SET_SIZE = 1;
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet(REGISTERED_PATH_SET_SIZE);

    private ZookeeperRegistryService() {
    }

    public static ZookeeperRegistryService getInstance() {
        if (null == instance) {
            synchronized (ZookeeperRegistryService.class) {
                if (null == instance) {
                    instance = new ZookeeperRegistryService();
                }
            }
        }
        return instance;
    }

    @Override
    public void register(InetSocketAddress address) {
        NetUtil.validAddress(address);
        String path = getRegisterPathByAddress(address);
        doRegister(path);
    }

    private boolean doRegister(String path) {
        if (checkExists(path)) {
            return false;
        }
        createParentIfNotPresent(path);
        // 先插入到zk再添加到缓存
        getClientInstance().createEphemeral(path, true);
        REGISTERED_PATH_SET.add(path);
        return true;
    }

    /**
     * 创建父节点
     * @param path （持久节点）
     */
    private void createParentIfNotPresent(String path) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
            String parent = path.substring(0, i);
            if (!checkExists(parent)) {
                getClientInstance().createPersistent(parent);
            }
        }
    }

    private boolean checkExists(String path) {
        return getClientInstance().exists(path);
    }

    @Override
    public void unregister(InetSocketAddress address) {
        NetUtil.validAddress(address);

        String path = getRegisterPathByAddress(address);
        getClientInstance().delete(path);
        REGISTERED_PATH_SET.remove(path);
    }

    /**
     * 从zk集群订阅
     * @param cluster
     * @param listener
     */
    @Override
    public void subscribe(String cluster, IZkChildListener listener) {
        if (null == cluster) {
            return;
        }
        String path = ROOT_PATH + cluster;
        if (!getClientInstance().exists(path)) {
            getClientInstance().createPersistent(path);
        }
        getClientInstance().subscribeChildChanges(path, listener);

        LISTENER_SERVICE_MAP.computeIfAbsent(cluster, a -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    @Override
    public void unsubscribe(String cluster, IZkChildListener listener) {
        if (null == cluster) {
            return;
        }
        String path = ROOT_PATH + cluster;
        if (getClientInstance().exists(path)) {
            getClientInstance().unsubscribeChildChanges(path, listener);

            LISTENER_SERVICE_MAP.computeIfPresent(cluster, (s, iZkChildListeners) ->
                    iZkChildListeners.stream()
                    .filter(eventListener -> !eventListener.equals(listener))
                    .collect(Collectors.toList()));
        }
    }

    /**
     * @param key the key
     * @return
     * @throws Exception
     */
    @Override
    public List<InetSocketAddress> lookup(String key) {
        String clusterName = getServiceGroup(key);
        if (null == clusterName) {
            return Collections.emptyList();
        }
        return doLookup(clusterName);
    }

    // visible for test.
    private List<InetSocketAddress> doLookup(String clusterName) {
        boolean exist = getClientInstance().exists(ROOT_PATH + clusterName);
        if (!exist) {
            return null;
        }
        if (!LISTENER_SERVICE_MAP.containsKey(clusterName)) {
            List<String> childClusterPath = getClientInstance().getChildren(ROOT_PATH + clusterName);
            refreshClusterAddressMap(clusterName, childClusterPath);
            subscribeCluster(clusterName);
        }
        return CLUSTER_ADDRESS_MAP.get(clusterName);
    }

    @Override
    public void close() throws Exception {
        getClientInstance().close();
    }

    private ZkClient getClientInstance() {
        if (zkClient == null) {
            synchronized (ZookeeperRegistryService.class) {
                if (null == zkClient) {
                    zkClient = buildZkClient("localhost:2181",
                            6000,
                            3000);
                }
            }
        }
        return zkClient;
    }

    // visible for test.
    ZkClient buildZkClient(String address, int sessionTimeout, int connectTimeout,String... authInfo) {
        ZkClient zkClient = new ZkClient(address, sessionTimeout, connectTimeout);
        if (!zkClient.exists(ROOT_PATH_WITHOUT_SUFFIX)) {
            zkClient.createPersistent(ROOT_PATH_WITHOUT_SUFFIX, true);
        }
        if (null != authInfo && authInfo.length == 2) {
            if (!StringUtils.isBlank(authInfo[0]) && !StringUtils.isBlank(authInfo[1])) {
                StringBuilder auth = new StringBuilder(authInfo[0]).append(":").append(authInfo[1]);
                zkClient.addAuthInfo("digest", auth.toString().getBytes());
            }
        }
        zkClient.subscribeStateChanges(new IZkStateListener() {

            @Override
            public void handleStateChanged(Watcher.Event.KeeperState keeperState) throws Exception {
                //ignore
            }

            @Override
            public void handleNewSession() throws Exception {
                recover();
            }

            @Override
            public void handleSessionEstablishmentError(Throwable throwable) throws Exception {
                //ignore
            }
        });
        return zkClient;
    }

    private void recover() {
        // recover Server
        if (!REGISTERED_PATH_SET.isEmpty()) {
            REGISTERED_PATH_SET.forEach(this::doRegister);
        }
        // recover client
        if (!LISTENER_SERVICE_MAP.isEmpty()) {
            Map<String, List<IZkChildListener>> listenerMap = new HashMap<>(LISTENER_SERVICE_MAP);
            for (Map.Entry<String, List<IZkChildListener>> listenerEntry : listenerMap.entrySet()) {
                List<IZkChildListener> iZkChildListeners = listenerEntry.getValue();
                if (CollectionUtils.isEmpty(iZkChildListeners)) {
                    continue;
                }
                for (IZkChildListener listener : iZkChildListeners) {
                    subscribe(listenerEntry.getKey(), listener);
                }
            }
        }
    }

    private void subscribeCluster(String cluster) {
        subscribe(cluster, (parentPath, currentChilds) -> {
            String clusterName = parentPath.replace(ROOT_PATH, "");
            if (CollectionUtils.isEmpty(currentChilds) && CLUSTER_ADDRESS_MAP.get(clusterName) != null) {
                CLUSTER_ADDRESS_MAP.remove(clusterName);
            } else if (!CollectionUtils.isEmpty(currentChilds)) {
                refreshClusterAddressMap(clusterName, currentChilds);
            }
        });
    }

    private void refreshClusterAddressMap(String clusterName, List<String> instances) {
        List<InetSocketAddress> newAddressList = new ArrayList<>();
        if (instances == null) {
            CLUSTER_ADDRESS_MAP.put(clusterName, newAddressList);
            return;
        }
        for (String path : instances) {
            try {
                String[] ipAndPort = path.split(NetUtil.IP_PORT_SPLIT_CHAR);
                newAddressList.add(new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1])));
            } catch (Exception e) {
                LOGGER.warn("The cluster instance info is error, instance info:{}", path);
            }
        }
        CLUSTER_ADDRESS_MAP.put(clusterName, newAddressList);
    }

    private String getClusterName() {
        return "default-cluster";
    }

    private String getRegisterPathByAddress(InetSocketAddress address) {
        return ROOT_PATH + getClusterName() + ZK_PATH_SPLIT_CHAR + NetUtil.toStringAddress(address);
    }

    public InetSocketAddress findRegisteredAddress() {
        String root = ROOT_PATH + getClusterName();
        List<String> address = getClientInstance().getChildren(root);
        if (CollectionUtils.isEmpty(address)) {
            return null;
        }
        String[] addr = address.get(0).split(NetUtil.IP_PORT_SPLIT_CHAR);
        return new InetSocketAddress(addr[0], Integer.parseInt(addr[1]));
    }
}
