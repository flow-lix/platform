package learn.platform.rpc.connection;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import learn.platform.rpc.client.RpcClientHandler;
import learn.platform.rpc.protocol.RpcDecoder;
import learn.platform.rpc.protocol.RpcEncoder;
import learn.platform.rpc.protocol.RpcRequest;
import learn.platform.rpc.protocol.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionManager {

    private static ConnectionManager instance = new ConnectionManager();

    private AtomicInteger roundRobin = new AtomicInteger(0);

    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    private volatile boolean isRunning = true;

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    private long connectionTimeoutMs = 6000;

    private static ThreadPoolExecutor clientConnectExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));

    private ConnectionManager() {
    }

    public static ConnectionManager getInstance() {
        return instance;
    }

    public RpcClientHandler chooseHandler() {
        int size = connectedHandlers.size();
        while (isRunning && size <= 0) {
            try {
                if (waitForHandler()) {
                    size = connectedHandlers.size();
                }
            } catch (InterruptedException e) {
                log.error("Wait for available connections be interrupted!", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        if (roundRobin.get() == size) {
            roundRobin.set(0);
        }
        return connectedHandlers.get(roundRobin.getAndIncrement() % size);
    }

    private NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    private void connectServerNode(final InetSocketAddress remoteAddress) {
        clientConnectExecutor.execute(() -> {
            Bootstrap bootstrap = new Bootstrap();
            final RpcClientHandler clientHandler = new RpcClientHandler();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcResponse.class))
                                    .addLast(clientHandler);
                        }
                    });
            try {
                bootstrap.connect(remoteAddress).addListener(channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        log.info("Success connect to remote address: {}", remoteAddress);
                        addHandler(clientHandler, remoteAddress);
                    }
                }).sync();
            } catch (InterruptedException e) {
                log.error("客户端连接被中断!", e);
                Thread.currentThread().interrupt();
            }
        });
    }

    private void addHandler(RpcClientHandler clientHandler, InetSocketAddress remoteAddress) {
        connectedHandlers.add(clientHandler);
        connectedServerNodes.put(remoteAddress, clientHandler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitForHandler() throws InterruptedException {
        try {
            lock.lock();
            return condition.await(connectionTimeoutMs, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        if (null == allServerAddress) {
            return;
        }
        if (!allServerAddress.isEmpty()) {
            Set<InetSocketAddress> newServerNodeSet = new HashSet<>();
            // update local serverNode cache
            for (String address : allServerAddress) {
                String[] array = address.split(":");
                if (array.length == 2) {
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    newServerNodeSet.add(new InetSocketAddress(host, port));
                }
            }
            // add new server node
            for (final InetSocketAddress address : newServerNodeSet) {
                if (!connectedServerNodes.values().contains(address)) {
                    connectServerNode(address);
                }
            }
            // close and remove invalid server nodes
            for (RpcClientHandler clientHandler : connectedHandlers) {
                SocketAddress address = clientHandler.getRemoteAddress();
                if (!newServerNodeSet.contains(address)) {
                    log.info("Remove invalid server node " + address);
                    RpcClientHandler handler = connectedServerNodes.get(address);
                    if (handler != null) {
                        handler.close();
                    }
                    connectedHandlers.remove(handler);
                    connectedServerNodes.remove(address);
                }
            }
        } else {
            // 所有节点挂掉
            log.error("No available server node. All server nodes are down !!!");
            for (final RpcClientHandler connectedServerHandler : connectedHandlers) {
                SocketAddress remotePeer = connectedServerHandler.getRemoteAddress();
                RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                handler.close();
                connectedServerNodes.remove(connectedServerHandler);
            }
            connectedHandlers.clear();
        }
    }

    public void stop() {
        isRunning = false;
        for (RpcClientHandler handler : connectedHandlers) {
            handler.close();
        }
        signalAvailableHandler();
        clientConnectExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }

}
