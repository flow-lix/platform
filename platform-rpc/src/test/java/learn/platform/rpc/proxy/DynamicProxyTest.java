package learn.platform.rpc.proxy;

import learn.platform.rpc.example.Hello;
import learn.platform.rpc.example.HelloServiceImpl;
import org.junit.Test;

import java.lang.reflect.Proxy;

/**
 * 动态代理测试
 */
public class DynamicProxyTest {

    @Test
    public void testProxy() {
        Hello hello = new HelloServiceImpl();

        Hello h = (Hello) Proxy.newProxyInstance(hello.getClass().getClassLoader(), new Class[]{Hello.class}, new ProxyObject(hello));

        System.out.println(h.say("zz"));
    }

}
