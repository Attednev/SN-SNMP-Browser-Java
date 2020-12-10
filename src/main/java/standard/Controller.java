package standard;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.soulwing.snmp.SnmpEvent;
import org.soulwing.snmp.VarbindCollection;
import scanner.DeviceProperties;
import scanner.SNMPBrowser;
import ui.buttons.TextButton;
import ui.inputField.IPField;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class Controller {
    public HBox textButtons;

    public HBox scanHBox;
    public HBox customOIDBox;
    public VBox menuVBox;
    public Button backButton;
    public TextField communityField;
    public TextField customOIDInput;
    public TextButton scanNetworkButton;
    public TextButton scanDeviceButton;
    public ListView<String> deviceList;
    public TableView<Pair<String, String>> propertyTable;
    public IPField ipField;

    private String currentDisplayedDevice = "";
    private boolean scanNetwork = true;
    private final ArrayList<DeviceProperties> devices = new ArrayList<>();

    public void initialize() throws IOException {
        this.setupScene();
        SNMPBrowser.initialize();
        SNMPBrowser.startTrapListener();
    }

    private void setupScene() {
        this.setDeviceListListener();
        this.initializePropertyTable();
    }

    private void setDeviceListListener() {
        this.deviceList.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                if (newVal != null) {
                    this.currentDisplayedDevice = newVal;
                    this.updatePropertyTable();
                }
            }
        );
    }

    private void initializePropertyTable() {
        double[] sizes = {150, 395};
        String[] texts = {"Mib", "Value"};
        String[] varNames = {"key", "value"};
        for (int i = 0; i < texts.length; i++) {
            TableColumn<Pair<String, String>, String> tc = new TableColumn<>(texts[i]);
            tc.setPrefWidth(sizes[i]);
            tc.setCellValueFactory(new PropertyValueFactory<>(varNames[i]));
            this.propertyTable.getColumns().add(tc);
        }
    }

    private void updatePropertyTable() {
    for (DeviceProperties device : this.devices) {
        if (device.getIp().equals(this.currentDisplayedDevice)) {
            this.propertyTable.getItems().clear();
            for (Map.Entry<String, String> entry : device.getProperties().entrySet()) {
                this.propertyTable.getItems().add(new Pair<>(entry.getKey(), entry.getValue()));
            }
            return;
        }
    }
}

    public void changeScanMode() {
        this.scanNetworkButton.setIsHighlighted(!this.scanNetworkButton.getIsHighlighted());
        this.scanDeviceButton.setIsHighlighted(!this.scanDeviceButton.getIsHighlighted());
        this.scanNetwork = this.scanNetworkButton.getIsHighlighted();
        this.ipField.setHasMask(this.scanNetwork);
    }

    public void changeTheme() {
        boolean isDarkMode = Main.getScene().getStylesheets().get(0).contains("dark");
        URL url = Controller.class.getResource("/" + (isDarkMode ? "light" : "dark") + "Mode.css");
        Main.getScene().getStylesheets().set(0, url.toString());
    }

    public void changeScene() {
        this.menuVBox.setVisible(!this.menuVBox.isVisible());
        this.scanHBox.setVisible(!this.scanHBox.isVisible());
        this.backButton.setVisible(this.scanHBox.isVisible());
        this.customOIDBox.setVisible(this.scanHBox.isVisible());
        this.propertyTable.getItems().clear();
        this.currentDisplayedDevice = "";
        if (menuVBox.isVisible()) {
            this.devices.clear();
            this.deviceList.getItems().clear();
        }
    }

    public void sendCustomRequest() {
        String request = this.customOIDInput.getText();
        String community = this.communityField.getText();
        SNMPBrowser.sendAsyncSNMPRequest(this.currentDisplayedDevice, community, request);
    }

    public void startSNMPProcess() {
        SNMPBrowser.onResponse(this::handleResponse);
        if (SNMPBrowser.startScan(this.ipField.getIP(),  this.communityField.getText(), this.ipField.getMask(), this.scanNetwork)) {
            this.changeScene();
        }
    }

    private void handleResponse(SnmpEvent<VarbindCollection> snmpEvent) {
        DeviceProperties properties = new DeviceProperties();
        VarbindCollection result = snmpEvent.getResponse().get();
        properties.setIp(result.get("ipAdEntAddr").asString());
        result.forEach(v -> properties.getProperties().put(v.getName().split("\\.")[0], v.asString()));

        Platform.runLater(() -> this.addPropertiesToList(properties));
        snmpEvent.getContext().close();
    }

    private void addPropertiesToList(DeviceProperties properties) {
        for (DeviceProperties d : this.devices) {
            if (d.getIp().equals(properties.getIp())) {
                d.getProperties().putAll(properties.getProperties());
                this.updatePropertyTable();
                return;
            }
        }
        this.devices.add(properties);
        this.deviceList.getItems().add(properties.getIp());
    }

}
