package learn.platform.registry.support;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.registry.NotifyListener;
import learn.platform.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static learn.platform.commons.constants.CommonConstants.APPLICATION_KEY;

public class AbstractRegistry implements Registry {

    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractRegistry.class);

    private Resource registeredResource;

    private File backupFile;

    private Properties properties;

    private Set<Resource> registered = ConcurrentHashMap.newKeySet();
    private ConcurrentMap<Resource, Set<NotifyListener>> subscribered = new ConcurrentHashMap<>();

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
        Objects.requireNonNull(resource, "Resource can't be null!");
        this.registeredResource = resource;
    }

    @Override
    public void register(Resource resource) {
        Objects.requireNonNull(resource, "Registry resource is null!");
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
        Objects.requireNonNull(resource, "resource can't null!");
        Objects.requireNonNull(listener, "listener can't null!");
        LOGGER.info("Subscribe url: {}", resource);
        Set<NotifyListener> listenerSet = subscribered.computeIfAbsent(resource, k -> ConcurrentHashMap.newKeySet());
        listenerSet.add(listener);
    }

    @Override
    public void unsubscribe(Resource resource, NotifyListener listener) {

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
}
