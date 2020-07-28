package learn.platform.rpc.client;

import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RpcFuture implements Future<Object> {

    private RpcRequest request;
    private RpcResponse response;

    private long startTime;
    private long responseTimeThreshold = 5000;

    private CountDownLatch countDownLatch;
    private ReentrantLock lock = new ReentrantLock();

    private List<AsyncRpcCallback> pendingCallbacks = new ArrayList<>();

    public RpcFuture(RpcRequest request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
        this.countDownLatch = new CountDownLatch(1);
    }

    public RpcFuture addCallback(AsyncRpcCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(AsyncRpcCallback callback) {
        final RpcResponse ret = this.response;
        RpcClient.submit(() -> {
            if (ret.isSuccess()) {
                callback.success(ret.getResult());
            } else {
                callback.fail(new RuntimeException("Response error, " + ret.getError()));
            }
        });
    }

    public void done(RpcResponse response) {
        this.response = response;
        countDownLatch.countDown();
        invokeCallbacks();

        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > responseTimeThreshold) {
            log.warn("Service response time is too slow, response id: " + response.getInvokeId() + "response time: " + responseTime);
        }
    }

    private void invokeCallbacks() {
        try {
            lock.lock();
            for (AsyncRpcCallback callback : pendingCallbacks) {
                callback.success(response);
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return countDownLatch.getCount() == 0;
    }

    @Override
    public Object get() throws InterruptedException {
        countDownLatch.await();
        if (this.response != null) {
            return response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        boolean success = countDownLatch.await(timeout, unit);
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }
}
