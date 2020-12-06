package standard;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.soulwing.snmp.*;
import scanner.DeviceProperties;
import ui.buttons.SlideButton;
import ui.buttons.TextButton;
import ui.inputField.NumberField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    @FXML
    private VBox menuVBox;
    @FXML
    private HBox scanHBox;
    @FXML
    private ListView<Label> deviceList;
    @FXML
    private TableView<Pair<String, String>> propertyTable;

    private final Mib mib = MibFactory.getInstance().newMib();
    private boolean scanNetwork = true;
    private final ArrayList<DeviceProperties> devices = new ArrayList<>();

    @FXML
    private void initialize() throws IOException {
        addDarkModeButton();
        addTextButtons();
        addAddressFields();
        loadMibModules();
        setDeviceListListener();

        TableColumn mi = new TableColumn("Mib");
        mi.setPrefWidth(200);
        mi.setCellValueFactory(
                new PropertyValueFactory<Pair<String, String>, String>("key"));
        TableColumn va = new TableColumn("Value");
        va.setPrefWidth(200);
        va.setCellValueFactory(
                new PropertyValueFactory<Pair<String, String>, String>("value"));

        propertyTable.getColumns().addAll(mi, va);


       /* TableColumn<String, String> n1   = new TableColumn<>("Customer");
        TableColumn<String, String> n2 = new TableColumn<>("Customer No");
        TableColumn<String, String> full = new TableColumn<>();
        full.getColumns().add(n1);
        full.getColumns().add(n2);
        this.propertyTable.getColumns().add(full);*/
    }

    // TODO: OUTSOURCE SNMP-FUNCTIONS? DUNNO
    // TODO: ADD OID INPUTBOX IN SCENE

    private void setDeviceListListener() {
        this.deviceList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Label> ov, Label oldVal, Label newVal) -> {
                    for (DeviceProperties device : this.devices) {
                        if (device.getIp().equals(newVal.getText())) {
                            // TODO: PRINT PROPERTIES
                            this.propertyTable.getItems().clear();
                            for (Map.Entry<String, String> entry : device.getProperties().entrySet()) {
                                this.propertyTable.getItems().add(new Pair<>(entry.getKey(), entry.getValue()));
                            }
                            break;
                        }
                    }

                    newVal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
                    if (oldVal != null) {
                        oldVal.setStyle("-fx-font-size: 16px");
                    }
                }
        );
    }

    private void loadMibModules() throws IOException {
        this.mib.load("IP-MIB");
        this.mib.load("HOST-RESOURCES-MIB");
        this.mib.load("SNMPv2-MIB");
        this.mib.load("IF-MIB");
    }

    private void addAddressFields() {
        this.addressContainer.getChildren().addAll(
            new NumberField(3), new Label("."), new NumberField(3), new Label("."),
            new NumberField(3), new Label("."), new NumberField(3));
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
        String path = "file:src/resources/" + (isDarkMode ? "dark" : "light") + "Mode.css";
        Main.getScene().getStylesheets().set(0, path);
    }

    @FXML
    private void backButtonPressed() {
        changeScene();
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

        // TODO: CHECK OVERFLOW
    /*    Label l1 = new Label("000.000.000.000");
        l1.setStyle("-fx-font-size: 16px");
        this.deviceList.getItems().add(l1);
        for (int i = 0; i < 20; i++) {
            Label l = new Label(i + "");
            l.setStyle("-fx-font-size: 16px");
            this.deviceList.getItems().add(l);
        }*/

        if (!community.equals("")) {
            String netmask = ((NumberField)this.subnetContainer.getChildren().get(1)).getText();
            if (!scanNetwork) {
                devices.clear();
                sendAsyncSNMPRequest(ip, community);
                changeScene();
            } else if (!netmask.equals("")) {
                devices.clear();
                scanNetwork(ip, Long.parseLong(netmask), community);
                changeScene();
            }
        }
    }

    private void changeScene() {
        this.menuVBox.setVisible(!this.menuVBox.isVisible());
        this.scanHBox.setVisible(!this.scanHBox.isVisible());
        this.backButton.setVisible(this.scanHBox.isVisible());
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

            HashMap<String, String> map = new HashMap<>();
            for (Varbind v : result) {
                String key = v.getName().split("\\.")[0];
                String value = v.asString();
                map.put(key, value);
            }
            String ip = String.format("%s", result.get("ipAdEntAddr"));

            DeviceProperties device = new DeviceProperties(ip, map);

            Platform.runLater(() -> {
                this.devices.add(device);

                Label label = new Label(ip);
                label.setStyle("-fx-font-size: 16px");
                this.deviceList.getItems().add(label);
            });
        } catch (SnmpException ignore) {
        } finally {
            snmpEvent.getContext().close();
        }
    }

}
