package learn.platform.remoting.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.state.ConnectionState;

public interface StateListener {

    void stateChanged(StateType changed);
}
