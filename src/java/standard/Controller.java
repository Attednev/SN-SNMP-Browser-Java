package standard;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.soulwing.snmp.*;
import ui.NumberField;
import ui.SlideButton;
import ui.TextButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Controller {
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

    private final Mib mib = MibFactory.getInstance().newMib();
    private boolean scanNetwork = true;

    @FXML
    private void initialize() throws IOException {
        addDarkModeButton();
        addTextButtons();
        addAddressFields();
        loadMibModules();
    }

    private void loadMibModules() throws IOException {
        this.mib.load("IP-MIB");
        this.mib.load("HOST-RESOURCES-MIB");
        this.mib.load("SNMPv2-MIB");
        this.mib.load("IF-MIB");
    }

    private void addAddressFields() {
        this.addressContainer.setPrefWidth(root.getPrefWidth() - textButtons.getPrefWidth());
        this.addressContainer.getChildren().addAll(new NumberField(3), createLabel("."), new NumberField(3),
                createLabel("."), new NumberField(3), createLabel("."), new NumberField(3));

        this.subnetContainer.setPrefWidth(root.getPrefWidth() - textButtons.getPrefWidth());
        this.subnetContainer.getChildren().addAll(createLabel("/"), new NumberField(2));
    }

    private Label createLabel(String str) {
        Label label = new Label(str);
        label.setAlignment(Pos.BOTTOM_CENTER);
        //label.setStyle("-fx-min-height: 40; -fx-font-size: 30; -fx-text-fill: black");
        return label;
    }

    private void addDarkModeButton() {
        double buttonWidth = 90;
        double buttonHeight = 35;
        SlideButton btn = new SlideButton(buttonWidth, buttonHeight);
        btn.onAction(() -> changeTheme(btn.isOn()));
        buttonContainer.getChildren().add(btn);
        textButtons.setPrefSize(root.getPrefWidth() - (buttonWidth + 5) * 2, root.getPrefHeight() - (buttonHeight + 5) * 2);
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
        textButtons.setPrefWidth(300);
    }

    private void changeTheme(boolean isDarkMode) {
        String path = "standard/" + (isDarkMode ? "dark" : "light") + "Mode.css";
        Main.getScene().getStylesheets().set(0, path);
    }

    @FXML
    public void startSNMPProcess() {

        String ip = "";
        String community = "";
        if (scanNetwork) {
            long netmask = 8;
            scanNetwork(ip, netmask);
        } else {
            sendAsyncSNMPRequest(ip, community);
        }

    }

    public void scanNetwork(String network, long netmask) {
        String[] split = network.split("\\.");
        long x = Integer.parseInt(split[0]);
        long y = Integer.parseInt(split[1]);
        long z = Integer.parseInt(split[2]);
        long w = Integer.parseInt(split[3]);
        long address = (1 << 24) * x + (1 << 16) * y + (1 << 8) * z + w;
        long tail = 1 << (32 - netmask);
        long nw = address / tail * tail;
        long broadcast = (address / tail + 1) * tail - 1;

        for (long add = nw + 1; add < broadcast; ++add) {
            sendAsyncSNMPRequest(getIP(add), "public");
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
        List<String> mibList = Arrays.asList("sysDescr", "sysUpTime");

        context.asyncGetNext(this::captureSNMPResponse, mibList);
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
