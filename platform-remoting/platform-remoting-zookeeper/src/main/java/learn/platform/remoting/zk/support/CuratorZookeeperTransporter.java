package learn.platform.remoting.zk.support;

import learn.platform.commons.Resource;
import learn.platform.remoting.zk.ZookeeperClient;

public class CuratorZookeeperTransporter extends AbstractZookeeperTransporter {

    @Override
    protected ZookeeperClient createZookeeperClient(Resource resource) {
        return new CuratorZookeeperClient(resource);
    }
}
