package learn.platform.remoting.zk;

import learn.platform.commons.Resource;

import java.util.List;

public interface ZookeeperClient {

    /**
     * 创建节点
     * @param path
     * @param ephemeral 是否是临时节点
     */
    void create(String path, boolean ephemeral);

    /**
     * 创建节点并初始化数据
     * @param path 节点路径
     * @param content 初始化数据
     * @param epemeral 是否临时节点
     */
    void create(String path, String content, boolean epemeral);

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

    List<String> addChildListener(String path, ChildListener childListener);

    void removeChildListener(String path, ChildListener listener);

    void close();
}
