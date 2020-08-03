package learn.platform.commons.util;

import java.net.InetSocketAddress;

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
}
