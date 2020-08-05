package learn.platform.remoting.zookeeper;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;

public interface ZookeeperTransporter {

    ZookeeperClient connect(UrlResource resource);
}
