package learn.platform.remoting.zookeeper;

import learn.platform.commons.Resource;

public interface ZookeeperTransporter {

    ZookeeperClient connect(Resource resource);
}
