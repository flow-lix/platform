package learn.platform.remoting.zookeeper.support;

import learn.platform.commons.Resource;
import learn.platform.remoting.zookeeper.ZookeeperClient;

public class CuratorZookeeperTransporter extends AbstractZookeeperTransporter {

    @Override
    protected ZookeeperClient createZookeeperClient(Resource resource) {
        return new CuratorZookeeperClient(resource);
    }
}
