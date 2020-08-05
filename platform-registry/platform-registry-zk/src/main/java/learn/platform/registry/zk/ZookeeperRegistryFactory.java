package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.registry.Registry;
import learn.platform.remoting.zookeeper.support.CuratorZookeeperTransporter;

public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    @Override
    public Registry getRegistry(Resource resource) {
        return new ZookeeperRegistry(new UrlResource(resource), new CuratorZookeeperTransporter());
    }
}
