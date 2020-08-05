package learn.platform.remoting.zookeeper;

import java.util.List;

public interface ZookeeperClient {

    /**
     * 创建节点
     * @param path
     * @param ephemeral 是否是临时节点
     */
    void create(String path, boolean ephemeral);

    void delete(String path);

    /**
     * 查询子节点
     * @param path
     * @return
     */
    List<String> getChildren(String path);

    /**
     * 客户端是否已建立连接
     * @return
     */
    boolean isConnected();

    String getContent(String path);

    void addStateListener(StateListener stateListener);
}
