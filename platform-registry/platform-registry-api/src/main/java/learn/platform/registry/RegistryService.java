package learn.platform.registry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 注册服务接口
 * @param <T> Child监听器
 */
public interface RegistryService<T> {

    /**
     * 注册服务
     * @param registerAddress
     */
    void register(InetSocketAddress registerAddress);

    /**
     * 注销服务
     * @param registerAddress
     */
    void unregister(InetSocketAddress registerAddress);

    /**
     * 订阅服务
     * @param cluster
     * @param listener
     */
    void subscribe(String cluster, T listener);

    /**
     * 取消订阅
     * @param cluster
     * @param listener
     */
    void unsubscribe(String cluster, T listener);

    /**
     * 查询服务列表
     * @param key
     * @return
     */
    List<InetSocketAddress> lookup(String key);

    /**
     * 关闭服务
     * @throws Exception
     */
    void close() throws Exception;

    default String getServiceGroup(String key) {
        return "default_group";
    }
}
