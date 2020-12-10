package scanner;

import javafx.application.Platform;
import org.soulwing.snmp.*;
import standard.Main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class SNMPBrowser {
    private final static Mib mib = MibFactory.getInstance().newMib();
    private static SnmpCallback<VarbindCollection> onResponseFunction;

    public static void initialize() throws IOException {
        SNMPBrowser.loadMibFiles();
        SNMPBrowser.createShutDownHook();
    }

    private static void createShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                SnmpFactory.getInstance().close();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    private static void loadMibFiles() throws IOException {
        BufferedReader fr = new BufferedReader(new InputStreamReader(SNMPBrowser.class.getResourceAsStream("/Mib-List.txt")));
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
        return SNMPBrowser.sendAsyncSNMPRequest(address, community, "sysName", "sysUpTime", "sysContact", "sysLocation", "sysDescr");
    }

    public static boolean sendAsyncSNMPRequest(String address, String community, String... oid) {
        if (address.equals("") || community.equals("")) {
            return false;
        }
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(System.getProperty("tnm4j.agent.address", address));
        target.setCommunity(System.getProperty("tnm4j.agent.community", community));

        SimpleSnmpTargetConfig config = new SimpleSnmpTargetConfig();
        config.setTimeout(2000);
        config.setRetries(1);

        SnmpContext context = SnmpFactory.getInstance().newContext(target, mib, config, null);

        ArrayList<String> list = new ArrayList<>();
        Collections.addAll(list, oid);
        list.add(0, "ipAdEntAddr");

        try {
            context.asyncGetNext(SNMPBrowser.onResponseFunction, list);
        } catch (IllegalArgumentException e) {
            return false;
        }
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
        SnmpListener listener = SnmpFactory.getInstance().newListener(10162, mib);
        listener.addHandler(event -> {
            StringBuilder message = new StringBuilder();
            event.getSubject().getVarbinds().asList().forEach(v ->
                    message.append("\t" + v.getName().split("\\.")[0] + " -> " + v.asString() + "\n"));
            Platform.runLater(() -> Main.alertBox(
                "From: " + event.getSubject().getPeer().getAddress() + "\n" +
                "Type: " + event.getSubject().getType() + "\n" +
                "Message:\n" + message.toString())
            );
            return true;
        });
    }

}
