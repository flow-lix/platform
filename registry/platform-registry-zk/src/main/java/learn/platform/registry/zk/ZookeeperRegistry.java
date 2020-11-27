package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.URLStrParser;
import learn.platform.commons.url.UrlResource;
import learn.platform.registry.NotifyListener;
import learn.platform.registry.support.FailbackRegistry;
import learn.platform.remoting.zk.ChildListener;
import learn.platform.remoting.zk.ZookeeperClient;
import learn.platform.remoting.zk.ZookeeperTransporter;
import learn.platform.rpc.exception.RpcException;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static learn.platform.commons.constants.CommonConstants.PATH_SEPARATOR;
import static learn.platform.commons.constants.RegistryConstants.CATEGORY_KEY;
import static learn.platform.commons.constants.RegistryConstants.DEFAULT_CATEGORY;

public class ZookeeperRegistry extends FailbackRegistry {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final String root = "/root";

    private final ZookeeperClient zkClient;

    private ConcurrentMap<Resource, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<>();

    public ZookeeperRegistry(Resource resource, ZookeeperTransporter transporter) {
        super(resource);

        zkClient = transporter.connect(resource);
        zkClient.addStateListener((state) -> {
            switch (state) {
                case RECONNECTED:
                    LOGGER.warn("重新获取发布者地址列表！");
                    break;
                case NEW_SESSION_CREATED:
                    LOGGER.warn("重新注册Provider并且重新订阅监听器");
                    break;
                case CONNECTED:
                case SUSPENDED:
                default:
                    throw new AssertionError(state);
            }
        });
    }

    @Override
    protected void doRegister(Resource resource) {
        try {
            zkClient.create(toUrlPath(resource), false);
        } catch (Exception e) {
            throw new RpcException("Failed to register " + resource + " to zk，cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doUnregister(Resource resource) {
        try {
            zkClient.delete(toUrlPath(resource));
        } catch (Exception e) {
            throw new RpcException("Failed to unregister " + resource + " to zk，cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doSubscribe(Resource resource, NotifyListener listener) {
        try {
            String path = toCategoriesPath(resource);

            ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.computeIfAbsent(resource, k -> new ConcurrentHashMap<>());
            ChildListener childListener = listeners.computeIfAbsent(listener,
                    k -> (parentPath, children) -> notify(resource, toResources(children), k));
            zkClient.create(path, false);
            List<String> children = zkClient.addChildListener(path, childListener);
            List<Resource> childList = new ArrayList<>();
            if (children != null) {
                childList.addAll(toResources(children));
            }
            notify(resource, childList, listener);
        } catch (Exception e) {
            throw new RpcException("Failed to subscribe " + resource + " to zookeeper " + getRegistryResource() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doUnsubscribe(Resource resource, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listenerMap = zkListeners.get(resource);
        if (MapUtils.isNotEmpty(listenerMap)) {
            ChildListener cListener = listenerMap.get(listener);
            zkClient.removeChildListener(toCategoriesPath(resource), cListener);
        }
    }

    private List<Resource> toResources(List<String> children) {
        List<Resource> urlResourceList = new ArrayList<>();
        for (String child : children) {
            urlResourceList.add(URLStrParser.parseEncodedStr(child));
        }
        return urlResourceList;
    }

    private String toUrlPath(Resource resource) {
        return toCategoriesPath(resource) + PATH_SEPARATOR +  UrlResource.encode(resource.toString());
    }

    /**
     * /root + /interfaceName + /category
     * @param resource
     * @return
     */
    private String toCategoriesPath(Resource resource) {
        String categories = resource.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        return toServicePath(resource) + PATH_SEPARATOR + categories;
    }

    private String toServicePath(Resource resource) {
        return root;
    }

    public boolean isAvailable() {
        return zkClient.isConnected();
    }

    @Override
    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            LOGGER.warn("Failed to close zookeeper client {}, cause: {}", getRegistryResource(), e.getMessage(), e);
        }
    }

    public List<Resource> lookup(UrlResource resource) {
        Objects.requireNonNull(resource, "lookup resource == null!");
        
        try {
            List<String> providers = zkClient.getChildren(toCategoriesPath(resource));
            return toResources(providers);
        } catch (Exception e) {
            throw new RpcException("Failed to lookup " + resource + " from zookeeper " + getRegistryResource() + ", cause: " + e.getMessage(), e);
        }
    }
}
