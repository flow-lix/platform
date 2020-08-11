package learn.platform.registry.support;

import learn.platform.commons.Resource;
import learn.platform.registry.NotifyListener;
import learn.platform.registry.Registry;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static learn.platform.commons.constants.CommonConstants.APPLICATION_KEY;

public class AbstractRegistry implements Registry {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistry.class);

    private File backupFile;

    private Properties properties;

    private Set<Resource> registered = ConcurrentHashMap.newKeySet();

    private ConcurrentMap<Resource, Set<NotifyListener>> subscribed = new ConcurrentHashMap<>();

    /**
     * consumer resource for providers resource
     */
    private ConcurrentMap<Resource, List<Resource>> notified = new ConcurrentHashMap<>();

    /**
     * 本地磁盘缓存文件
     */
    private File file;

    private Resource registryResource;

    public AbstractRegistry(Resource resource) {
        setResource(resource);

        String defaultFileName = System.getProperty("user.home") + "/dubbo-registry-" +
                resource.getParameter(APPLICATION_KEY) + resource.getClusterAddress() + ".cache";

        File file = new File(defaultFileName);
        if (!file.exists() && file.getParentFile() != null && !file.getParentFile().exists() &&
                !file.getParentFile().mkdir()) {
            throw new IllegalArgumentException("Failed to create directory + file.getParentFile() + !");
        }
        this.backupFile = file;
        loadProperties();
        notify(resource.getClusterAddress());
    }

    private void notify(List<String> clusterAddress) {
    }

    private void loadProperties() {
        try (InputStream is = new FileInputStream(this.backupFile)) {
            properties.load(is);
            LOGGER.info("Load registry cache file {}, data: {}",backupFile, properties);
        } catch (Exception e) {
            LOGGER.warn("Failed to load registry cache file {}", backupFile, e);
        }
    }

    public void setResource(Resource resource) {
        Objects.requireNonNull(resource);
        this.registryResource = resource;
    }

    @Override
    public void register(Resource resource) {
        Objects.requireNonNull(resource);
        LOGGER.info("Register resource: {}", resource);
        registered.add(resource);
    }

    @Override
    public void unregister(InetSocketAddress registerAddress) {

    }

    /**
     * 添加到缓存
     * @param resource
     * @param listener
     */
    @Override
    public void subscribe(Resource resource, NotifyListener listener) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(listener);
        LOGGER.info("Subscribe url: {}", resource);
        Set<NotifyListener> listenerSet = subscribed.computeIfAbsent(resource, k -> ConcurrentHashMap.newKeySet());
        listenerSet.add(listener);
    }

    @Override
    public void unsubscribe(Resource resource, NotifyListener listener) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(listener);
        Set<NotifyListener> listenerSet = subscribed.get(resource);
        if (listenerSet != null) {
            listenerSet.remove(listener);
        }
    }

    /**
     *
     * @param cResource 一个消费者
     * @param pResources 多个发布信息
     * @param notifyListener
     */
    protected void notify(Resource cResource, List<Resource> pResources, NotifyListener notifyListener) {
        Objects.requireNonNull(cResource);
        Objects.requireNonNull(notifyListener);
        if (CollectionUtils.isEmpty(pResources)) {
            LOGGER.warn("Ignore empty notify resource for subscribe resource {}", cResource);
        }
        LOGGER.info("Notify resource for subscribe resource {}, providerResource: {}", cResource, pResources);

        notified.put(cResource, pResources);

        // 通知所有发布者
        notifyListener.notify(pResources);
        saveProperties();
    }

    private void saveProperties() {
        if (file == null) {
            return;
        }
    }

    @Override
    public List<InetSocketAddress> lookup(String key) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    public Set<Resource> getRegistered() {
        return Collections.unmodifiableSet(registered);
    }

    public String getRegistryAddress() {
        return registryResource.getBackupAddress();
    }

    public Resource getRegistryResource() {
        return registryResource;
    }

    public Map<Resource, Set<NotifyListener>> getSubscribed() {
        return Collections.unmodifiableMap(subscribed);
    }

    public void destroy() {
    }
}
