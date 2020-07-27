package learn.platform.rpc.protocol;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcResponse {

    private String invokeId;
    private Object result;
    private String error;

    public boolean isSuccess() {
        return error == null;
    }
}
