package learn.platform.example;

import learn.platform.config.ApplicationConfig;
import learn.platform.config.RegistryConfig;
import learn.platform.config.ServiceConfig;

import java.util.concurrent.CountDownLatch;

public class ProviderApplication {

    public static void main(String[] args) throws Exception {
        ServiceConfig<DemoServiceImpl> serviceConfig = new ServiceConfig<>();
        serviceConfig.setInterface(DemoService.class);
        serviceConfig.setImpl(new DemoServiceImpl());
        serviceConfig.setApplication(new ApplicationConfig("demo-service-application"));
        serviceConfig.setRegistry(new RegistryConfig("zookeeper://localhost:2181"));

        serviceConfig.export();

        new CountDownLatch(1).await();
    }
}
