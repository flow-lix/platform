package learn.platform.remoting.zookeeper;

import learn.platform.remoting.zookeeper.support.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;

/**
 * Zookeeper状态类型
 */
public enum StateType implements StateListener {

    /**
     * 会话丢失
     */
    SESSION_LOST(0),
    /**
     * 已连接
     */
    CONNECTED(1),
    /**
     * 已重连
     */
    RECONNECTED(2),
    /**
     * 挂起
     */
    SUSPENDED(3),
    /**
     * 建立新会话
     */
    NEW_SESSION_CREATED(4);

    /**
     * 状态编码
     */
    private final int type;

    StateType(int type) {
        this.type = type;
    }

    @Override
    public void stateChanged(StateType changed) {

    }
}
