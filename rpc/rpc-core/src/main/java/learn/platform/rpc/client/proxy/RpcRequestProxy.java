package learn.platform.rpc.client.proxy;

import learn.platform.rpc.client.TestClient;
import learn.platform.rpc.protocol.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

@Slf4j
public class RpcRequestProxy implements InvocationHandler {

    private TestClient client;
    private Object target;

    public RpcRequestProxy() {
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setMethodParamsType(method.getParameterTypes());
        request.setMethodParams(args);

        Object ret = new TestClient().sendSyncRequest(request);
        log.info("结果: {}", ret);
        return ret;
    }
}
