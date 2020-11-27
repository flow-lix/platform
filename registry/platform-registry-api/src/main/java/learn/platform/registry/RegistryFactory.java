package learn.platform.registry;

import learn.platform.commons.Resource;

public interface RegistryFactory {

    Registry createRegistry(Resource resource);
}
