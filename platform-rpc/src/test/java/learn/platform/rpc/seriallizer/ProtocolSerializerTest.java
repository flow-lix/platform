package learn.platform.rpc.seriallizer;

import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.serializer.ProtoSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class ProtocolSerializerTest {

    @Test
    public void testEncoderAndDecoder() throws Exception {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setMethodName("methodName");

        byte[] data = ProtoSerializer.serializer(request);

        RpcRequest req = ProtoSerializer.deserializer(data, RpcRequest.class);

        Assert.assertEquals(req.getRequestId(), request.getRequestId());
    }
}
