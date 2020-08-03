package learn.platform.commons.repository.redis;

import learn.platform.commons.repository.Repository;
import learn.platform.commons.repository.RepositoryFactory;

public class RedisRepositoryFactory implements RepositoryFactory {

    @Override
    public Repository getRepository() {
        return new RedisRepository();
    }
}
