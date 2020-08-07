package learn.platform.commons.url;

import learn.platform.commons.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlResource implements Resource, Serializable {

    private String identify;

    private String protocol;
    private String username;
    private String password;
    private String host;
    private int port;
    private String path;
    private final Map<String, Object> parameters = new HashMap<>();

    private transient volatile String address;

    public UrlResource(String protocol, String username, String password, String host, int port, String path) {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public static UrlResource valueOf(String link) {
        return URLStrParser.parseDecodedStr(link);
    }

    public String getAddress() {
        if (address == null) {
            address = host + ":" + port;
        }
        return address;
    }

    @Override
    public String getIdentifyId() {
        StringBuilder buffer = new StringBuilder();
        if (StringUtils.isNotEmpty(username)) {
            buffer.append(username);
            if (StringUtils.isNotEmpty(password)) {
                buffer.append(":").append(password);
            }
            buffer.append("@");
        }
        buffer.append(getAddress());
        return buffer.toString();
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
        Object obj = parameters.get(key);
        if (obj == null) {
            return defaultVal;
        }
        return (int) obj;
    }

    @Override
    public String getParameter(String key, String defaultVal) {
        return null;
    }

    @Override
    public String getAuthority() {
        return null;
    }

    @Override
    public String getBackupAddress() {
        return getAddress();
//        return StringUtils.join(getClusterAddress(), ",");
    }

    public void putParameters(Map<String, Object> parameters) {
        if (parameters != null) {
            this.parameters.putAll(parameters);
        }
    }
}
