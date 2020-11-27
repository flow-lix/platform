package learn.platform.rpc.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        String request = "RpcServerHandler channelRegistered!";
        ByteBuf byteBuf = Unpooled.buffer(request.length());
//        byteBuf.writeBytes(request.getBytes());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("建立Channel连接成功!");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("收到消息: {}", msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        RpcServer.submit(() ->{
            RpcResponse response = new RpcResponse();
            response.setInvokeId(rpcRequest.getRequestId());
            try {
                response.setResult(RpcServer.handler(rpcRequest));
            } catch (Exception e) {
                log.error("调用方法失败, rpcRequest: {}", rpcRequest, e);
                response.setError(e.getMessage());
            }
            ctx.writeAndFlush(response).addListener(channelFuture -> log.info("远程调用成功!,  requestId: {}", rpcRequest.getRequestId())
            );
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端异常!", cause);
    }
}
