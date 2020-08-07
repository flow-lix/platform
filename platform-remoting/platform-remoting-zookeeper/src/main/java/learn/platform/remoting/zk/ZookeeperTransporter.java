package learn.platform.remoting.zk;

import learn.platform.commons.Resource;
import learn.platform.commons.url.UrlResource;

public interface ZookeeperTransporter {

    ZookeeperClient connect(Resource resource);
}
