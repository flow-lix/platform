package learn.platform.rpc.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Channel channel;
    private SocketAddress remoteAddress;

    private ConcurrentMap<String, RpcFuture> pendingFutures = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        remoteAddress = channel.remoteAddress();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) throws Exception {
        String requestId = rpcResponse.getInvokeId();
        RpcFuture future = pendingFutures.remove(requestId);
        if (future != null) {
            future.done(rpcResponse);
        }
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture future = new RpcFuture(request);
        CountDownLatch latch = new CountDownLatch(1);
        pendingFutures.put(request.getRequestId(), future);
        channel.writeAndFlush(request).addListener(channelFuture -> {
            if (channelFuture.isSuccess()) {
                latch.countDown();
            } else {
                log.error("客户端消息发送失败: {}", request);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error("发送消息失败!", e);
            Thread.currentThread().interrupt();
        }
        return future;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端异常!", cause);
    }
}
