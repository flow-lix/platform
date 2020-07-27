package learn.platform.rpc;

import learn.platform.rpc.example.Hello;
import learn.platform.rpc.example.HelloServiceImpl;
import learn.platform.rpc.server.RpcServer;
import learn.platform.rpc.zookeeper.ServiceRegistry;

public class RpcProviderBootstrap {

    public static void main(String[] args) {
        ServiceRegistry registryCenter = new ServiceRegistry("localhost:2181");

        String serverAddress = "localhost:8000";
        RpcServer server = new RpcServer(registryCenter, serverAddress, 8000);

        Hello hello = new HelloServiceImpl();
        server.addService("HelloService", hello);
        try {
            server.start();
        } catch (InterruptedException ignore) {
            Thread.currentThread().interrupt();
        }
    }
}
