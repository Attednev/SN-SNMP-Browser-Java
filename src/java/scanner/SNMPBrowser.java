package scanner;

import javafx.scene.control.Alert;
import org.soulwing.snmp.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class SNMPBrowser {
    private final static Mib mib = MibFactory.getInstance().newMib();
    private static SnmpCallback<VarbindCollection> onResponseFunction;

    public static void initialize() throws IOException {
        SNMPBrowser.loadMibFiles();
    }

    private static void loadMibFiles() throws IOException {
        BufferedReader fr = new BufferedReader(new FileReader("src/resources/Mib-List.txt"));
        String module;
        while ((module = fr.readLine()) != null) {
            try {
                SNMPBrowser.mib.load(module);
            } catch (FileNotFoundException e) {
                System.out.println("Module " + module + " not found");
            }
        }
    }

    private static boolean scanNetwork(String network, long netmask, String community) {
        String[] split = network.split("\\.");
        long address = (1 << 24) * Integer.parseInt(split[0]) +
                (1 << 16) * Integer.parseInt(split[1]) +
                (1 << 8) * Integer.parseInt(split[2]) +
                Integer.parseInt(split[3]);
        long tail = 1 << (32 - netmask);
        long lastAddress = (address / tail + 1) * tail;

        for (long add = address; add < lastAddress; add++) {
            SNMPBrowser.sendAsyncSNMPRequest(SNMPBrowser.getIP(add), community);
        }
        return true;
    }

    private static String getIP(long add) {
        long octet = (1 << 8);
        long w = add % octet;
        add /= octet;
        long z = add % octet;
        add /= octet;
        long y = add % octet;
        add /= octet;
        long x = add;
        return (x + "." + y + "." + z + "." + w);
    }

    private static boolean sendAsyncSNMPRequest(String address, String community) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(System.getProperty("tnm4j.agent.address", address));
        target.setCommunity(System.getProperty("tnm4j.agent.community", community));

        SnmpContext context = SnmpFactory.getInstance().newContext(target, mib);

        context.asyncGetNext(SNMPBrowser.onResponseFunction, "sysName", "sysUpTime", "ipAdEntAddr");
        return true;
    }


    public static boolean startScan(String ip, String community, String netmask, boolean scanNetwork) {
        if (ip == null || community.equals("") || (scanNetwork && netmask.equals(""))) {
            return false;
        } else if (scanNetwork) {
            return SNMPBrowser.scanNetwork(ip, Long.parseLong(netmask), community);
        }
        return SNMPBrowser.sendAsyncSNMPRequest(ip, community);
    }

    public static void onResponse(SnmpCallback<VarbindCollection> onResponseFunction) {
        SNMPBrowser.onResponseFunction = onResponseFunction;
    }

    public static void startTrapListener() {
        new Thread(() -> {
            SnmpListener listener = SnmpFactory.getInstance().newListener(10162, mib);
            try {
                listener.addHandler(event -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("SNMP-Trap");
                    alert.setHeaderText(null);
                    alert.setContentText("" + event);
                    alert.show();
                    return true;
                });
                Thread.sleep(60000L);
            } catch (InterruptedException e) {
                System.err.println("<SNMP-Browser> Stopped listening for Trap packages due to an interrupt");
            } finally {
                listener.close();
            }
        });
    }

}
