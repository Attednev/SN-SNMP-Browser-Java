package scanner;

import java.util.HashMap;
import java.util.Map;

public class DeviceProperties {

    private String ip;
    private Map<String, String> properties;

    public DeviceProperties(String ip, HashMap<String, String> properties) {
        this.ip = ip;
        this.properties = properties;
    }

    public String getIp() {
        return this.ip;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

}
