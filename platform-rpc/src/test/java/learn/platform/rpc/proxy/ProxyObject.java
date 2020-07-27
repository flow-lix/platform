package learn.platform.rpc.proxy;

import learn.platform.rpc.example.Hello;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyObject implements InvocationHandler {

    private Hello hello;

    public ProxyObject(Hello hello) {
        this.hello = hello;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before invoke!");

        Object ret = method.invoke(hello, args);

        System.out.println("After invoke!");
        return ret;
    }
}
