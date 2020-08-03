package learn.platform.commons.repository.zookeeper;

import learn.platform.commons.repository.Repository;
import learn.platform.commons.repository.RepositoryFactory;

public class ZookeeperRepositoryFactory implements RepositoryFactory {

    @Override
    public Repository getRepository() {
        return new ZookeeperRepository();
    }
}
