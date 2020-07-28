package learn.platform.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import learn.platform.rpc.example.Hello;
import learn.platform.rpc.example.HelloServiceImpl;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.reflect.FastClass;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TestServer {

    private static ConcurrentMap<String, Object> registeredBeans = new ConcurrentHashMap<>();

    static RpcResponse processRequest(RpcRequest request) {
        String className    = request.getClassName();
        Object bean = registeredBeans.get(className);
        if (null == bean) {
            return null;
        }
        String methodName     = request.getMethodName();
        Object[] methodParams = request.getMethodParams();
        Class<?>[] paramTypes = request.getMethodParamsType();

        Object ret = null;
        try {
            ret = FastClass.create(bean.getClass())
                    .getMethod(methodName, paramTypes)
                    .invoke(bean, methodParams);
        } catch (InvocationTargetException e) {
            log.error("远程调用异常!", e);
        }
        RpcResponse response = new RpcResponse();
        response.setInvokeId(request.getRequestId());
        response.setResult(ret);
        return response;
    }

    public void bind(int port) throws Exception {
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            registerBean(new HelloServiceImpl());
            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel arg0) throws Exception {
            arg0.pipeline()
                    .addLast(new StringDecoder())
                    .addLast(new StringEncoder())
                    .addLast(new TestServerHandler());
        }
    }

    private static void registerBean(Object object) {
        registeredBeans.put(Hello.class.getName(), new HelloServiceImpl());
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        int port = 8000;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                // 采用默认值
            }
        }
        new TestServer().bind(port);
    }
}
