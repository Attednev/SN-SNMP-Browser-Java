package standard;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.soulwing.snmp.Varbind;
import org.soulwing.snmp.VarbindCollection;
import scanner.DeviceProperties;
import scanner.SNMPBrowser;
import ui.buttons.TextButton;
import ui.inputField.IPField;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    public HBox textButtons;

    public HBox scanHBox;
    public HBox customOIDBox;
    public VBox menuVBox;
    public Button backButton;
    public TextField communityField;
    public TextField customOIDInput;
    public ListView<String> deviceList;
    public TableView<Pair<String, String>> propertyTable;
    public IPField ipField;

    private String currentDisplayedDevice = "";
    private boolean scanNetwork = true;
    private final ArrayList<DeviceProperties> devices = new ArrayList<>();

// --- SETUP --- //
    public void initialize() throws IOException, URISyntaxException {
        this.addInitialSceneElements();
        SNMPBrowser.initialize();
        SNMPBrowser.startTrapListener();
    }

    private void addInitialSceneElements() {
        this.addTextButtons();
        this.setDeviceListListener();
        this.initializePropertyTable();
    }

    private void addTextButtons() {
        String[] captions = {"Scan network", "Scan device"};
        for (AtomicInteger i = new AtomicInteger(0); i.get() < captions.length; i.set(i.get() + 1)) {
            TextButton btn = new TextButton(captions[i.get()]);
            btn.setOnMouseClicked(e -> {
                this.scanNetwork = (i.get() == 0);

                //this.subnetParent.setVisible(this.scanNetwork);
                //for (Node t : this.textButtons.getChildren()) {
                //    ((TextButton)t).clear();
                //}
                btn.highlight();
            });
            if (i.get() == 0) {
                btn.highlight();
            }
            this.textButtons.getChildren().add(btn);
        }
    }

    private void setDeviceListListener() {
        this.deviceList.getSelectionModel().selectedItemProperty().addListener(
            (ObservableValue<? extends String> ov, String oldVal, String newVal) -> {
                if (newVal != null) {
                    //newVal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
                    this.currentDisplayedDevice = newVal;//.getText();
                }
                if (oldVal != null) {
                    //oldVal.setStyle("-fx-font-size: 16px");
                }
                this.updatePropertyTable();
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

// --- -------- ------------ -------- --- //
    public void changeTheme() {
        String path = "file:src/resources/" +
                (Main.getScene().getStylesheets().get(0).contains("light") ? "dark" : "light") + "Mode.css";
        Main.getScene().getStylesheets().set(0, path);
    }

    public void changeScene() {
        this.menuVBox.setVisible(!this.menuVBox.isVisible());
        this.scanHBox.setVisible(!this.scanHBox.isVisible());
        this.backButton.setVisible(this.scanHBox.isVisible());
        this.customOIDBox.setVisible(this.scanHBox.isVisible());
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

    public void sendCustomRequest() {
        String request = this.customOIDInput.getText();
        String community = this.communityField.getText();
        SNMPBrowser.sendAsyncSNMPRequest(this.currentDisplayedDevice, community, request);
    }

    public void startSNMPProcess() {
        SNMPBrowser.onResponse(snmpEvent -> {
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
                for (int i = 0; i < this.deviceList.getItems().size(); i++) {
                    if (this.deviceList.getItems().get(i).equals(ip)) {
                        this.devices.get(i).getProperties().putAll(device.getProperties());
                        if (this.currentDisplayedDevice.equals(ip)) {
                            this.updatePropertyTable();
                        }
                        return;
                    }
                }
                this.deviceList.getItems().add(ip);
                this.devices.add(device);
            });
            snmpEvent.getContext().close();
        });


        String ip = this.ipField.getIP();//this.getIPFromScene();
        String community = this.communityField.getText();
        String netmask = this.ipField.getMask();///((NumberField)this.subnetContainer.getChildren().get(1)).getText();
        this.devices.clear();
        this.deviceList.getItems().clear();
        if (SNMPBrowser.startScan(ip, community, netmask, this.scanNetwork)) {
            this.changeScene();
        }

    }

}
