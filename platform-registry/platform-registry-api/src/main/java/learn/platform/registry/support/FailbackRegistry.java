package learn.platform.registry.support;

import learn.platform.commons.Resource;
import learn.platform.commons.thread.NamedThreadFactory;
import learn.platform.commons.timer.HashedWheelTimer;
import learn.platform.registry.NotifyListener;

import java.util.concurrent.TimeUnit;

/**
 * 出故障时自动恢复
 */
public abstract class FailbackRegistry extends AbstractRegistry {

    /**
     * 重试间隔
     */
    private final int retryPeriod = 5 * 1000;
    private final HashedWheelTimer retryTimer;

    public FailbackRegistry(Resource resource) {
        super(resource);
        retryTimer = new HashedWheelTimer(new NamedThreadFactory("RpcRegistryRetryTimer", true),
                retryPeriod, TimeUnit.MILLISECONDS, 128);
    }

    @Override
    public void register(Resource resource) {
        super.register(resource);
        try {
            doRegister(resource);
        } catch (Exception e) {
            LOGGER.error("注册失败!", e);
        }
        // Record a failed registration request to a failed list, retry regularly
//        addFailedRegistered(resource);
    }

    @Override
    public void subscribe(Resource resource, NotifyListener listener) {
        super.subscribe(resource, listener);
        try {
            doSubscribe(resource, listener);
        } catch (Exception e) {
            LOGGER.error("Failed to subscribe {}, cause: {}", resource, e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe(Resource resource, NotifyListener listener) {
        super.unsubscribe(resource, listener);
        try {
            doUnsubscribe(resource, listener);
        } catch (Exception e) {
            LOGGER.error("Failed to unsubscribe " + resource + ", waiting for retry, cause: " + e.getMessage(), e);
        }
    }

    protected abstract void doRegister(Resource resource);

    protected abstract void doUnregister(Resource resource);

    protected abstract void doSubscribe(Resource resource, NotifyListener listener);

    protected abstract void doUnsubscribe(Resource resource, NotifyListener listener);

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public String getServiceGroup(String key) {
        return null;
    }
}
