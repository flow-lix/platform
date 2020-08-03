package learn.platform.registry.zk;

public class ZookeeperConfig {

    public static final String ZK_PATH_SPLIT_CHAR = "/";
    public static final String FILE_ROOT_REGISTRY = "registry";
    public static final String FILE_CONFIG_SPLIT_CHAR = ".";
    public static final String REGISTRY_CLUSTER = "cluster";
    public static final String REGISTRY_TYPE = "zk";
    public static final String SERVER_ADDR_KEY = "serverAddr";
    public static final String AUTH_USERNAME = "username";
    public static final String AUTH_PASSWORD = "password";
    public static final String SESSION_TIME_OUT_KEY = "sessionTimeout";
    public static final String CONNECT_TIME_OUT_KEY = "connectTimeout";

    // registry.zk.
    public static final String FILE_CONFIG_KEY_PREFIX = FILE_ROOT_REGISTRY + FILE_CONFIG_SPLIT_CHAR + REGISTRY_TYPE
            + FILE_CONFIG_SPLIT_CHAR;

    // /registry/zk
    public static final String ROOT_PATH_WITHOUT_SUFFIX = ZK_PATH_SPLIT_CHAR + FILE_ROOT_REGISTRY + ZK_PATH_SPLIT_CHAR
            + REGISTRY_TYPE;

    // /registry/zk/
    public static final String ROOT_PATH = ROOT_PATH_WITHOUT_SUFFIX + ZK_PATH_SPLIT_CHAR;
    
}
