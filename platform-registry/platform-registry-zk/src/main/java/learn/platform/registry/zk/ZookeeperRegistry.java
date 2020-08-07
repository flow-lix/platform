package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.registry.NotifyListener;
import learn.platform.registry.support.FailbackRegistry;
import learn.platform.remoting.zk.ZookeeperClient;
import learn.platform.remoting.zk.ZookeeperTransporter;
import learn.platform.rpc.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static learn.platform.commons.constants.CommonConstants.PATH_SEPARATOR;
import static learn.platform.commons.constants.RegistryConstants.CATEGORY_KEY;
import static learn.platform.commons.constants.RegistryConstants.DEFAULT_CATEGORY;

public class ZookeeperRegistry extends FailbackRegistry {

    public static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private static final String root = "/root/";

    private final ZookeeperClient zkClient;

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

    private String toUrlPath(Resource resource) {
        String cate = resource.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
        return root + cate + PATH_SEPARATOR + resource.toString();
    }

    @Override
    protected void doRegister(Resource resource) {
        try {
            zkClient.create(toUrlPath(resource), true);
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
    protected void doSubscriber(Resource resource, NotifyListener listener) {
        String path = toCategoriesPath(resource);
        zkClient.create(path, false);
    }

    private String toCategoriesPath(Resource resource) {
        String categories;
        categories = resource.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);

        return toServicePath(resource) + PATH_SEPARATOR + categories;
    }

    private String toServicePath(Resource resource) {
        return root + resource.getInterfaceName();
    }



}
