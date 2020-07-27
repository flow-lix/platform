package learn.flow.commons;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

public abstract class AbstractZkClient {

    private CuratorFramework zkClient;


    public AbstractZkClient(CuratorFramework curatorFramework) {
        zkClient = CuratorFrameworkFactory.builder()
                .connectString()
                .retryPolicy()
                .connectionTimeoutMs()
                .sessionTimeoutMs()
                .build();

    }

    protected void init() {
        initZkRootNode();

        listenerMasterNode();

        listenerSlaveNode();
    }

    protected void initZkRootNode() {

    }


    protected void listenerMasterNode() {

    }

    private void listenerSlaveNode() {
    }



}
