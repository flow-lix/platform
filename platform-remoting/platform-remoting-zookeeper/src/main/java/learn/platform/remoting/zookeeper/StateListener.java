package learn.platform.remoting.zookeeper;


public interface StateListener {

    void stateChanged(StateType changed);
}
