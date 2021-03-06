package scanner;

import java.util.HashMap;

public class DeviceProperties {

    private String ip;
    private final HashMap<String, String> properties = new HashMap<>();

    public DeviceProperties() {}

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return this.ip;
    }

    public HashMap<String, String> getProperties() {
        return this.properties;
    }

}
