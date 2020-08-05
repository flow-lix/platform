package learn.platform.remoting.zookeeper.support;

import learn.platform.commons.Resource;
import learn.platform.remoting.zookeeper.StateListener;
import learn.platform.remoting.zookeeper.StateType;
import learn.platform.remoting.zookeeper.ZookeeperClient;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractZookeeperClient implements ZookeeperClient {

    protected int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    protected int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;

    static final Charset CHARSET =  Charset.forName("UTF-8");

    private final Resource resource;

    private Set<StateListener> sessionListeners = ConcurrentHashMap.newKeySet();

    private final Set<String> persistentExistNodePath = ConcurrentHashMap.newKeySet();

    public AbstractZookeeperClient(Resource resource) {
        this.resource = resource;
    }

    protected Set<String> getPersistentExistNodePath() {
        return persistentExistNodePath;
    }

    @Override
    public void addStateListener(StateListener stateListener) {
        this.sessionListeners.add(stateListener);
    }

    @Override
    public abstract void create(String path, boolean persistent);

    protected abstract boolean checkExists(String path);

    @Override
    public void delete(String path) {

    }

    public void stateChanged(StateType stateType) {
        for (StateListener listener : getSessionListeners()) {
            listener.stateChanged(stateType);
        }
    }

    public Set<StateListener> getSessionListeners() {
        return sessionListeners;
    }
}
