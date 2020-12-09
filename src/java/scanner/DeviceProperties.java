package scanner;

import java.util.HashMap;

public class DeviceProperties {

    private final String ip;
    private final HashMap<String, String> properties;

    public DeviceProperties(String ip, HashMap<String, String> properties) {
        this.ip = ip;
        this.properties = properties;
    }

    public String getIp() {
        return this.ip;
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

}
