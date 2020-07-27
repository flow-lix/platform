package learn.platform.rpc;

import learn.platform.rpc.client.RpcClient;
import learn.platform.rpc.example.Hello;
import learn.platform.rpc.zookeeper.ServiceDiscovery;
import org.junit.Test;

public class BenchmarkTest {

    @Test
    public void testServiceInvoke() throws InterruptedException {
        ServiceDiscovery discovery = new ServiceDiscovery("localhost:2181");
        RpcClient client = new RpcClient();

//        CompletableFuture<Object> future = CompletableFuture.runAsync()
        Thread t = new Thread(() -> {
            Hello hello = RpcClient.createProxy(Hello.class);
            hello.say("🐕");
            System.out.println();
        });
        t.start();

        t.join();
        System.out.println("finished!");
    }
}
