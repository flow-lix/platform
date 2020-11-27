package learn.platform.remoting.zk.support;

import learn.platform.commons.Resource;
import learn.platform.commons.constants.CommonConstants;
import learn.platform.remoting.zk.ChildListener;
import learn.platform.remoting.zk.StateType;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class CuratorZookeeperClient extends AbstractZookeeperClient<CuratorZookeeperClient.CuratorWatcherImpl, CuratorZookeeperClient.CuratorWatcherImpl> {

    private static final Logger logger = LoggerFactory.getLogger(CuratorZookeeperClient.class);

    private static final String ZK_SESSION_EXPIRE_KEY = "zk.session.expire";

    private final CuratorFramework zkClient;

    private ConcurrentMap<String, TreeCache> treeCacheMap = new ConcurrentHashMap<>();

    public CuratorZookeeperClient(Resource resource) {
        super(resource);
        try {
            int timeout = resource.getParameter(CommonConstants.TIMEOUT_KEY, DEFAULT_CONNECTION_TIMEOUT_MS);
            int sessionExpireMs = resource.getParameter(ZK_SESSION_EXPIRE_KEY, DEFAULT_SESSION_TIMEOUT_MS);
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().
                    connectString(resource.getBackupAddress())
                    .retryPolicy(new RetryNTimes(1, 1000))
                    .connectionTimeoutMs(timeout)
                    .sessionTimeoutMs(sessionExpireMs);
            String authority = resource.getAuthority();
            if (authority != null && authority.length() > 0) {
                builder = builder.authorization("digest", authority.getBytes());
            }
            zkClient = builder.build();
            zkClient.getConnectionStateListenable().addListener(new CuratorConnectionStateListener(timeout, sessionExpireMs));
            zkClient.start();

            if (!zkClient.blockUntilConnected(timeout, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("Zookeeper not connect!");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void create(String path, boolean persistent) {
        if (persistent) {
            // 创建持久节点
            if (getPersistentExistNodePath().contains(path)) {
                return;
            }
            if (checkExists(path)) {
                getPersistentExistNodePath().add(path);
                return;
            }
            try {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                getPersistentExistNodePath().add(path);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        } else {
            createEphemeral(path);
        }
    }

    /**
     * 创建临时节点
     * @param path
     */
    private void createEphemeral(String path) {
        try {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
            logger.error("节点已存在!");
            try {
                zkClient.delete().deletingChildrenIfNeeded().forPath(path);
            } catch (KeeperException.NoNodeException ignore) {

            } catch (Exception ex) {
                throw new IllegalStateException(e);
            }
            createEphemeral(path);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void create(String path, String data, boolean ephemeral) {
        if (checkExists(path)) {
            delete(path);
        }
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        if (ephemeral) {
            createEphemeral(path, dataBytes);
        } else {
            try {
                zkClient.create().creatingParentsIfNeeded().forPath(path, dataBytes);
            } catch (KeeperException.NodeExistsException e) {
                try {
                    zkClient.setData().forPath(path, dataBytes);
                } catch (Exception e1) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            } catch (Exception e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
    }

    private void createEphemeral(String path, byte[] dataByte) {
        try {
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, dataByte);
        } catch (KeeperException.NoNodeException e) {
            delete(path);
            createEphemeral(path, dataByte);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            zkClient.delete().forPath(path);
        } catch (KeeperException.NoNodeException ignore) {

        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> getChildren(String path) {
        try {
            return zkClient.getChildren().forPath(path);
        } catch (KeeperException.NoNodeException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected CuratorWatcherImpl createTargetChildListener(String path, ChildListener childListener) {
        return new CuratorWatcherImpl(this, path, childListener);
    }

    @Override
    public List<String> addTargetChildrenListener(String path, CuratorWatcherImpl childListener) {
        try {
            return zkClient.getChildren().usingWatcher(childListener).forPath(path);
        } catch (KeeperException.NodeExistsException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    protected void removeTargetChildListener(String path, CuratorWatcherImpl watcherImpl) {
        watcherImpl.unWatch();
    }

    @Override
    public void addTargetDataListener(String path, CuratorWatcherImpl watcher,  Executor executor) {
        try {
            TreeCache treeCache = TreeCache.newBuilder(zkClient, path).setCacheData(false).build();
            treeCacheMap.putIfAbsent(path, treeCache);
            if (executor == null) {
                treeCache.getListenable().addListener(watcher);
            } else {
                treeCache.getListenable().addListener(watcher, executor);
            }
            treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException("Add treeCache listener for path: " + path, e);
        }
    }

    @Override
    public boolean checkExists(String path) {
        try {
             if (zkClient.checkExists().forPath(path) != null) {
                 return true;
             }
        } catch (Exception ignore) {}
        return false;
    }

    @Override
    public boolean isConnected() {
        return zkClient.getZookeeperClient().isConnected();
    }

    @Override
    public String getContent(String path) {
        try {
            byte[] dataBytes = zkClient.getData().forPath(path);
            return (dataBytes == null || dataBytes.length == 0) ? null : new String(dataBytes, StandardCharsets.UTF_8);
        } catch (KeeperException.NoNodeException ignore) {
            // ignore
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return null;
    }

    @Override
    protected void doClose() {
        zkClient.close();
    }

    public CuratorFramework getClient() {
        return zkClient;
    }

    class CuratorConnectionStateListener implements ConnectionStateListener {
        private static final long UNKNOWN_SESSION_ID = -1;
        private long lastSessionId;

        private long timeout;
        private long sessionExpireMs;

        CuratorConnectionStateListener(long timeout, long sessionExpireMs) {
            this.timeout = timeout;
            this.sessionExpireMs = sessionExpireMs;
        }

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            long sessionId = UNKNOWN_SESSION_ID;
            try {
                sessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
            } catch (Exception e) {
                logger.warn("Curator client state changed, but failed to get the related zk session instance.");
            }

            if (newState == ConnectionState.LOST) {
                logger.warn("Curator zk session " + Long.toHexString(lastSessionId) + " expired.");
                CuratorZookeeperClient.this.stateChanged(StateType.SESSION_LOST);
            } else if (newState == ConnectionState.SUSPENDED) {
                logger.warn("Curator zk connection of session " + Long.toHexString(sessionId) + " timed out. " +
                        "connection timeout value is " + timeout + ", session expire timeout value is " + sessionExpireMs);
                CuratorZookeeperClient.this.stateChanged(StateType.SUSPENDED);
            } else if (newState == ConnectionState.CONNECTED) {
                lastSessionId = sessionId;
                logger.info("Curator zk client instance initiated successfully, session id is " + Long.toHexString(sessionId));
                CuratorZookeeperClient.this.stateChanged(StateType.CONNECTED);
            } else if (newState == ConnectionState.RECONNECTED) {
                if (lastSessionId == sessionId && sessionId != UNKNOWN_SESSION_ID) {
                    logger.warn("Curator zk connection recovered from connection lose, " +
                            "reuse the old session " + Long.toHexString(sessionId));
                    CuratorZookeeperClient.this.stateChanged(StateType.RECONNECTED);
                } else {
                    logger.warn("New session created after old session lost, " +
                            "old session " + Long.toHexString(lastSessionId) + ", new session " + Long.toHexString(sessionId));
                    lastSessionId = sessionId;
                    CuratorZookeeperClient.this.stateChanged(StateType.NEW_SESSION_CREATED);
                }
            }
        }
    }

    public static class CuratorWatcherImpl implements CuratorWatcher, TreeCacheListener {
        private CuratorZookeeperClient zkClient;
        private String path;
        private ChildListener childListener;

        public CuratorWatcherImpl() {
        }

        public CuratorWatcherImpl(CuratorZookeeperClient zkClient, String path, ChildListener childListener) {
            this.zkClient = zkClient;
            this.path = path;
            this.childListener = childListener;
        }

        @Override
        public void process(WatchedEvent event) throws Exception {
            if (childListener != null) {
                childListener.childChange(path, zkClient.getChildren(path));
            }
        }

        @Override
        public void childEvent(CuratorFramework curatorFramework, TreeCacheEvent treeCacheEvent) throws Exception {

        }

        public void unWatch() {
            childListener = null;
        }
    }
}
