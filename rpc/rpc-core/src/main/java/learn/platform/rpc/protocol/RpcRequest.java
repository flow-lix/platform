package learn.platform.rpc.protocol;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RpcRequest {

    private String requestId;
    private String className;
    private String methodName;
    private Class<?>[] methodParamsType;
    private Object[] methodParams;

}
