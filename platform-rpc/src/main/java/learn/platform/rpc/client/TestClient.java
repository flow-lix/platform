package learn.platform.rpc.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import learn.platform.rpc.client.proxy.RpcRequestProxy;
import learn.platform.rpc.example.Hello;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TestClient {

    private Channel channel;

    /**
     * 等待响应的请求
     */
    private static ConcurrentMap<String, RpcFuture> pendingRequest = new ConcurrentHashMap<>();

    public static void receiveResponse(RpcResponse response) {
        log.info("4.收到远程调用结果: {}", response);
        RpcFuture future = pendingRequest.get(response.getInvokeId());
        if (null == future) {
            return;
        }
        future.done(response);
    }

    /**
     * 发送同步请求
     * @param request
     * @return
     * @throws InterruptedException
     */
    public Object sendSyncRequest(RpcRequest request) throws InterruptedException{
        log.info("1.发送同步方法调用");
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRequest.put(request.getRequestId(), rpcFuture);

        getNewChannel(8000, "localhost").writeAndFlush(JSON.toJSONString(request))
                .addListener(future -> {
        });
        return rpcFuture.get();
    }

    public Channel getNewChannel(int port, String host) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline()
                                .addLast(new StringDecoder())
                                .addLast(new StringEncoder())
                                .addLast(new TestClientHandler());
                    }
                });
        // 发起异步连接操作
        ChannelFuture f = b.connect(host, port).sync();
        return f.channel();
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Hello proxy = (Hello) Proxy.newProxyInstance(Hello.class.getClassLoader(), new Class[]{Hello.class}, new RpcRequestProxy());
        proxy.say("proxy");
    }
}
