package learn.platform.remoting.zk;

import java.util.List;

public interface ChildListener {

    void childChange(String path, List<String> children);
}
