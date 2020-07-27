package learn.flow.commons.repository.zookeeper;

import learn.flow.commons.repository.Repository;
import learn.flow.commons.repository.RepositoryFactory;

public class ZookeeperRepositoryFactory implements RepositoryFactory {

    @Override
    public Repository getRepository() {
        return new ZookeeperRepository();
    }
}
