package standard;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.soulwing.snmp.*;
import ui.NumberField;
import ui.SlideButton;
import ui.TextButton;

import java.io.IOException;

public class Controller {
    @FXML
    private Button backButton;
    @FXML
    private VBox root;
    @FXML
    private HBox buttonContainer;
    @FXML
    private HBox textButtons;
    @FXML
    private HBox addressContainer;
    @FXML
    private HBox subnetContainer;
    @FXML
    private VBox subnetParent;
    @FXML
    private TextField communityField;

    private final Mib mib = MibFactory.getInstance().newMib();
    private boolean scanNetwork = true;
    private VBox menuVBox;
    private VBox scanVBox;

    @FXML
    private void initialize() throws IOException {
        addDarkModeButton();
        addTextButtons();
        addAddressFields();
        loadMibModules();
        menuVBox = ((VBox)this.root.getChildren().get(1));
    }

    private void loadMibModules() throws IOException {
        this.mib.load("IP-MIB");
        this.mib.load("HOST-RESOURCES-MIB");
        this.mib.load("SNMPv2-MIB");
        this.mib.load("IF-MIB");
    }

    private void addAddressFields() {
        this.addressContainer.getChildren().addAll(
                new NumberField(3), new Label("."),
                new NumberField(3), new Label("."),
                new NumberField(3), new Label("."),
                new NumberField(3));
        this.subnetContainer.getChildren().addAll(new Label("/"), new NumberField(2));
    }

    private void addDarkModeButton() {
        SlideButton btn = new SlideButton(90, 35);
        btn.onAction(() -> changeTheme(btn.isOn()));
        buttonContainer.getChildren().add(btn);
    }

    private void addTextButtons() {
        TextButton btnNetwork = new TextButton("Scan network", 300, 75);
        TextButton btnDevice = new TextButton("Scan device", 300, 75);
        btnNetwork.setOnMouseClicked(e -> {
            this.scanNetwork = true;
            subnetParent.setVisible(true);
            btnNetwork.highlight();
            btnDevice.clear();
        });
        btnDevice.setOnMouseClicked(e -> {
            this.scanNetwork = false;
            subnetParent.setVisible(false);
            btnDevice.highlight();
            btnNetwork.clear();
        });
        btnNetwork.highlight();
        textButtons.getChildren().addAll(btnNetwork, btnDevice);
    }

    private void changeTheme(boolean isDarkMode) {
        String path = "standard/" + (isDarkMode ? "dark" : "light") + "Mode.css";
        Main.getScene().getStylesheets().set(0, path);
    }

    @FXML
    private void backButtonPressed() {
        changeScene(false);
    }

    @FXML
    private void startSNMPProcess() {
        String[] ipParts = new String[7];
        for (int i = 0; i < ipParts.length; i++) {
            Node node = this.addressContainer.getChildren().get(i);
            String content = node instanceof Label ? ((Label) node).getText() : ((NumberField) node).getText();
            if (content.equals("")) {
                return;
            }
            ipParts[i] = content;
        }
        String ip = String.join("", ipParts);
        String community = this.communityField.getText();

        if (!community.equals("")) {
            String netmask = ((NumberField)this.subnetContainer.getChildren().get(1)).getText();
            if (!scanNetwork) {
                sendAsyncSNMPRequest(ip, community);
                changeScene(true);
            } else if (!netmask.equals("")) {
                scanNetwork(ip, Long.parseLong(netmask), community);
                changeScene(true);
            }
        }
    }

    private void changeScene(boolean toScanMenu) {
        if (toScanMenu) {
            setScanVBox();
        }
        this.backButton.setVisible(toScanMenu);
        this.root.getChildren().set(1, toScanMenu ? scanVBox : menuVBox);
    }

    private void setScanVBox() {
        scanVBox = new VBox();
        scanVBox.getChildren().add(new Label("test"));
    }

    private void scanNetwork(String network, long netmask, String community) {
        String[] split = network.split("\\.");
        long x = Integer.parseInt(split[0]);
        long y = Integer.parseInt(split[1]);
        long z = Integer.parseInt(split[2]);
        long w = Integer.parseInt(split[3]);
        long address = (1 << 24) * x + (1 << 16) * y + (1 << 8) * z + w;
        long tail = 1 << (32 - netmask);
        long nw = address / tail * tail;
        long broadcast = (address / tail + 1) * tail - 1;

        for (long add = nw + 1; add < broadcast; add++) {
            sendAsyncSNMPRequest(getIP(add), community);
        }
    }

    private String getIP(long add) {
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

    private void sendAsyncSNMPRequest(String address, String community) {
        SimpleSnmpV2cTarget target = new SimpleSnmpV2cTarget();
        target.setAddress(System.getProperty("tnm4j.agent.address", address));
        target.setCommunity(System.getProperty("tnm4j.agent.community", community));

        SnmpContext context = SnmpFactory.getInstance().newContext(target, mib);

        context.asyncGetNext(this::captureSNMPResponse, "sysName", "sysUpTime", "ipAdEntAddr");
    }

    private void captureSNMPResponse(SnmpEvent<VarbindCollection> snmpEvent) {
        try {
            VarbindCollection result = snmpEvent.getResponse().get();
            System.out.format("%s -> %s uptime %s\n",
                    result.get("ipAdEntAddr"),
                    result.get("sysName"),
                    result.get("sysUpTime"));
        } catch (SnmpException ex) {
            System.out.println(" -> no response or error");
        } finally {
            snmpEvent.getContext().close();
        }
    }

}
