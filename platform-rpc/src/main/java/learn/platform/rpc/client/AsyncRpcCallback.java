package learn.platform.rpc.client;

public interface AsyncRpcCallback {

    void success(Object result);

    void fail(Exception e);
}
