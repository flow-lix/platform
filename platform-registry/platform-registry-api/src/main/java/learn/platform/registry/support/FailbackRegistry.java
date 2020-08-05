package learn.platform.registry.support;

import learn.platform.commons.Resource;
import learn.platform.commons.thread.NamedThreadFactory;
import learn.platform.commons.timer.HashedWheelTimer;

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

    protected abstract void doRegister(Resource resource);

    protected abstract void doUnregister(Resource resource);

//    protected abstract void doSubscriber(Resource resource, )

    @Override
    public String getServiceGroup(String key) {
        return null;
    }
}
