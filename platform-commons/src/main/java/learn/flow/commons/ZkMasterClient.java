package learn.flow.commons;

public class ZkMasterClient extends AbstractZkClient {

    private static ZkMasterClient zkMasterClient;

    private ZkMasterClient() {}

    public static ZkMasterClient getInstance() {
        if (zkMasterClient == null) {
            synchronized (ZkMasterClient.class) {
                if (zkMasterClient == null) {
                    zkMasterClient = new ZkMasterClient();
                    zkMasterClient.init();
                }
            }
        }
        return zkMasterClient;
    }

    @Override
    void init() {
        super.init();

    }

    private void initZkRootNode() {
        createMasterNode();
        createSlaveNode();
        createDeadNode();
    }

    private void createMasterNode() {
        zkMasterClient.
    }

    private void createDeadNode() {
    }

    private void createSlaveNode() {
    }

}
