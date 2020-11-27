package learn.platform.rpc.client;

import io.netty.util.concurrent.DefaultThreadFactory;
import learn.platform.rpc.client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {

    private static ThreadPoolExecutor rpcClientExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new DefaultThreadFactory("rpcClientExecutor"));

    static void submit(Runnable runnable) {
        rpcClientExecutor.submit(runnable);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceClass) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new ObjectProxy<>());
    }
}
