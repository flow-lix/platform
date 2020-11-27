package learn.platform.registry.zk;

import learn.platform.commons.Resource;
import learn.platform.registry.Registry;
import learn.platform.remoting.zk.support.CuratorZookeeperTransporter;

public class ZookeeperRegistryFactory extends AbstractRegistryFactory {

    @Override
    public Registry createRegistry(Resource resource) {
        return new ZookeeperRegistry(resource, new CuratorZookeeperTransporter());
    }
}
