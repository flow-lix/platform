package learn.platform.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.concurrent.DefaultThreadFactory;
import learn.platform.rpc.annotation.Provider;
import learn.platform.rpc.protocol.RpcDecoder;
import learn.platform.rpc.protocol.RpcEncoder;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import learn.platform.rpc.zookeeper.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup worker = new NioEventLoopGroup();

    private final int port;
    private ServerBootstrap bootstrap;

    private ServiceRegistry serviceRegistry;
    private String serverAddress;

    private static ExecutorService executorService = new ThreadPoolExecutor(20, 200,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new DefaultThreadFactory("RpcInvokeExecutor"));

    /** 提供的服务 */
    private static Map<String, Object> serviceBeans = new HashMap<>();

    public RpcServer(ServiceRegistry serviceRegistry, String serverAddress, int port) {
        this.serviceRegistry = serviceRegistry;
        this.serverAddress = serverAddress;
        this.port = port;
    }

    public void addService(String serviceName, Object serviceBean) {
        if (!serviceBeans.containsKey(serviceName)) {
            log.debug("Load service: {}", serviceName);
            serviceBeans.put(serviceName, serviceBean);
        }
    }

    public void start() throws InterruptedException {
        this.bootstrap = new ServerBootstrap();
        try {
            this.bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<ServerSocketChannel>() {
                        @Override
                        protected void initChannel(ServerSocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new RpcDecoder(RpcRequest.class))
                                    .addLast(new RpcEncoder(RpcResponse.class))
                                    .addLast(new RpcServerHandler());
                        }
                    });
            ChannelFuture channelFuture = this.bootstrap.bind("localhost", port).sync();
            log.info("Rpc server started in port: [{}]!", port);

            // 服务注册
            if (serviceRegistry != null) {
                serviceRegistry.register(serverAddress);
            }
            channelFuture.channel().closeFuture().sync();
        } finally {
            this.boss.shutdownGracefully();
            this.worker.shutdownGracefully();
        }
    }


    public static Object handler(RpcRequest rpcRequest) throws Exception {
        String className = rpcRequest.getClassName();
        Object serviceBean = serviceBeans.get(className);

        Class<?>   serviceClass = serviceBean.getClass();
        String     methodName   = rpcRequest.getMethodName();
        Object[]   methodParams = rpcRequest.getMethodParams();
        Class<?>[] paramTypes   = rpcRequest.getMethodParamsType();

        FastClass serviceFastClass = FastClass.create(serviceClass);

        FastMethod method = serviceFastClass.getMethod(methodName, paramTypes);
        return method.invoke(serviceBean, methodParams);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Map<String, Object> annotations = applicationContext.getBeansWithAnnotation(Provider.class);
        if (!annotations.isEmpty()) {
            for (Object bean : annotations.values()) {
                String interfaceName = bean.getClass().getAnnotation(Provider.class).value().getName();
                log.debug("Load server bean: {}", interfaceName);
                serviceBeans.put(interfaceName, bean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    public static void submit(Runnable runnable) {
        executorService.execute(runnable);
    }
}
