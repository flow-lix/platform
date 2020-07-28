package learn.platform.rpc.client;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

@Slf4j
public class TestClientHandler extends SimpleChannelInboundHandler<String> {

    private static final Logger logger = Logger
            .getLogger(TestClientHandler.class.getName());

    private final ByteBuf firstMessage;

    private Channel channel;
    private SocketAddress remoteAddress;

    private ConcurrentMap<String, RpcFuture> pendingFutures = new ConcurrentHashMap<>();

    /**
     * Creates a client-side handler.
     */
    public TestClientHandler() {
        byte[] req = "QUERY TIME ORDER".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        RpcResponse response = JSON.parseObject(msg, RpcResponse.class);
        if (response.isSuccess()) {
            TestClient.receiveResponse(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 释放资源
        logger.warning("Unexpected exception from downstream : "
                + cause.getMessage());
        ctx.close();
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
