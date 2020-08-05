package learn.platform.commons.url;

import learn.platform.commons.Resource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UrlResource implements Resource {

    private String identify;

    private final Resource resource;

    public UrlResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String getIdentifyId() {
        return null;
    }

    @Override
    public List<String> getClusterAddress() {
        return null;
    }

    @Override
    public String getParameter(String applicationKey) {
        return null;
    }

    @Override
    public int getParameter(String key, int defaultVal) {
        return 0;
    }

    @Override
    public String getParameter(String key, String defaultVal) {
        return null;
    }

    @Override
    public String getAuthority() {
        return null;
    }

    public String getBackupAddress() {
        return StringUtils.join(getClusterAddress(), ",");
    }
}
