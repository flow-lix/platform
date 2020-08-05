package learn.platform.registry;

import learn.platform.commons.Resource;

public interface RegistryFactory {

    Registry getRegistry(Resource resource);
}
