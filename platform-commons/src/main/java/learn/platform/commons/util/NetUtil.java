package learn.platform.commons.util;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

public class NetUtil {

    public static final String IP_PORT_SPLIT_CHAR = ":";

    public static void validAddress(InetSocketAddress address) {
        if (null == address.getHostName() || address.getPort() == 0) {
            throw new IllegalArgumentException("无效的地址: " + address);
        }
    }

    public static String toStringAddress(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    public static int getAvailablePort() {
        try (Socket socket = new Socket()) {
            socket.bind(null);
            return socket.getLocalPort();
        } catch (Exception e) {
            return randomPort();
        }
    }

    private static final int RND_PORT_START = 30000;
    private static final int RND_PORT_RANGE = 10000;

    private static int randomPort() {
        return RND_PORT_START + ThreadLocalRandom.current().nextInt(RND_PORT_RANGE);
    }
}
