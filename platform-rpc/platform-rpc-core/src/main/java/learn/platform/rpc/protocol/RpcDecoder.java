package learn.platform.rpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import learn.platform.rpc.serializer.ProtoSerializer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class RpcDecoder extends ByteToMessageDecoder {

    /**
     * Response
     */
    private final Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        log.info("开始解码请求...");
        if (in.readableBytes() < 4) {
            return;
        }
        int bodyLength = in.readInt();
        if (in.readableBytes() < bodyLength) {
            in.markReaderIndex();
        }
        byte[] body = new byte[bodyLength];
        in.readBytes(body);

        list.add(ProtoSerializer.deserializer(body, genericClass));
    }

}
