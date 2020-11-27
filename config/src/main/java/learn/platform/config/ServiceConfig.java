package learn.platform.config;

import java.util.Objects;

/**
 * 服务发布配置
 */
public class ServiceConfig<T> {

    private String interfaceName;
    private Class<?> interfaceClass;

    /**
     * 接口实现类
     */
    private T impl;

    private String applicationId;

    private RegistryConfig registryConfig;

    private ApplicationConfig applicationConfig;

    private volatile boolean exported;

    public void export() {
        if (exported) {
            return;
        }
        exported = true;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterface(Class<?> interfaceClass) {
        Objects.requireNonNull(interfaceClass);
        if (!interfaceClass.isInterface()) {
            throw new IllegalStateException("The interface class " + interfaceClass + " is not interface!");
        }
        this.interfaceClass = interfaceClass;
        this.interfaceName = interfaceClass.getName();
    }

    public T getImpl() {
        return impl;
    }

    public void setImpl(T impl) {
        this.impl = impl;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }

    public void setRegistry(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    public void setApplication(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

}
