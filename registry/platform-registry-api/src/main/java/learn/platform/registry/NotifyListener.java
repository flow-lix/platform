package learn.platform.registry;

import learn.platform.commons.Resource;

import java.util.List;

public interface NotifyListener {

    /**
     * 当收到服务变化时触发
     * @param urls 注册信息
     */
    void notify(List<Resource> urls);
}
