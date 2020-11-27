package learn.platform.rpc.client.proxy;

import learn.platform.rpc.client.RpcClientHandler;
import learn.platform.rpc.client.RpcFuture;
import learn.platform.rpc.connection.ConnectionManager;
import learn.platform.rpc.protocol.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T> implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setMethodParams(args);
        request.setMethodParamsType(method.getParameterTypes());

        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler();
        RpcFuture future = handler.sendRequest(request);
        return future.get();
    }
}
