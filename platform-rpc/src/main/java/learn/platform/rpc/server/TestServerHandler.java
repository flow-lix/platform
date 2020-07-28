package learn.platform.rpc.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        log.info("2.收到远程调用请求");
        RpcRequest request = JSON.parseObject(msg, RpcRequest.class);
        RpcResponse response = TestServer.processRequest(request);

        log.info("3.发送方法调用结果: {}", response);
        ctx.writeAndFlush(JSON.toJSONString(response))
                .addListener(future -> log.info("发送成功!"));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
