package learn.platform.commons;

import java.util.List;

public interface Resource {

    /**
     * 查询资源唯一id
     * @return
     */
    String getIdentifyId();

    String getInterfaceName();
    /**
     * 查询集群地址
     * @return 地址列表
     */
    List<String> getClusterAddress();

    String getBackupAddress();

    String getParameter(String applicationKey);

    /**
     * 查询参数
     * @param key
     * @param defaultVal
     * @return
     */
    int getParameter(String key, int defaultVal);

    String getParameter(String key, String defaultVal);

    /**
     * 查询认证信息
     * @return
     */
    String getAuthority();

}
