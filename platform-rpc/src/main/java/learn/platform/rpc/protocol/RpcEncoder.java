package learn.platform.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import learn.platform.rpc.serializer.ProtoSerializer;

public class RpcEncoder extends MessageToByteEncoder {

    private final Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object message, ByteBuf byteBuf) throws Exception {
        if (genericClass.isInstance(message)) {
            byte[] body = ProtoSerializer.serializer(message);
            byteBuf.writeInt(body.length);
            byteBuf.writeBytes(body);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
