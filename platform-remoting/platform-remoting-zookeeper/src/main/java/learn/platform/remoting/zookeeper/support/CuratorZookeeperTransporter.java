package learn.platform.remoting.zookeeper.support;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;
import learn.platform.remoting.zookeeper.ZookeeperClient;

public class CuratorZookeeperTransporter extends AbstractZookeeperTransporter {

    @Override
    protected ZookeeperClient createZookeeperClient(UrlResource resource) {
        return new CuratorZookeeperClient(resource);
    }
}
