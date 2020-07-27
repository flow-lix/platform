package learn.flow.commons.repository.redis;

import learn.flow.commons.repository.Repository;
import learn.flow.commons.repository.RepositoryFactory;

public class RedisRepositoryFactory implements RepositoryFactory {

    @Override
    public Repository getRepository() {
        return new RedisRepository();
    }
}
