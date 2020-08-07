package learn.platform.remoting.zk.support;

import learn.platform.commons.Resource;
import learn.platform.remoting.zk.ChildListener;
import learn.platform.remoting.zk.StateListener;
import learn.platform.remoting.zk.StateType;
import learn.platform.remoting.zk.ZookeeperClient;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public abstract class AbstractZookeeperClient<TargetChildListener, TargetDataListener> implements ZookeeperClient {

    protected int DEFAULT_CONNECTION_TIMEOUT_MS = 5 * 1000;
    protected int DEFAULT_SESSION_TIMEOUT_MS = 60 * 1000;

    static final Charset CHARSET =  Charset.forName("UTF-8");

    private final Resource resource;

    private volatile boolean closed;

    private Set<StateListener> sessionListeners = ConcurrentHashMap.newKeySet();

    private final Set<String> persistentExistNodePath = ConcurrentHashMap.newKeySet();

    private ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<>();

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

    public abstract boolean checkExists(String path);


    public void stateChanged(StateType stateType) {
        for (StateListener listener : getSessionListeners()) {
            listener.stateChanged(stateType);
        }
    }

    public Set<StateListener> getSessionListeners() {
        return sessionListeners;
    }

    public List<String> addChildListener(String path, ChildListener childListener) {
        ConcurrentMap<ChildListener, TargetChildListener> targetMap = childListeners.computeIfAbsent(path, p -> new ConcurrentHashMap<>());
        TargetChildListener targetChildListener = targetMap.computeIfAbsent(childListener, k -> createTargetChildListener(path, childListener));
        return addTargetChildrenListener(path, targetChildListener);
    }

    protected abstract TargetChildListener createTargetChildListener(String path, ChildListener childListener);

    public void removeChildListener(String path, ChildListener childListener) {
        ConcurrentMap<ChildListener, TargetChildListener> targetMap = childListeners.get(path);
        if (targetMap != null) {
            removeTargetChildListener(path, targetMap.get(childListener));
        }
    }

    protected abstract void removeTargetChildListener(String path, TargetChildListener childListener);

    public void close() {
        if (closed) {
            return;
        }
        doClose();
    }

    protected abstract void doClose();

    public abstract List<String> addTargetChildrenListener(String path, TargetChildListener childListener);

    public abstract void addTargetDataListener(String path, TargetDataListener watcher, Executor executor);
}
