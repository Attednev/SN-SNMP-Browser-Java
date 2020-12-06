package standard;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.soulwing.snmp.SnmpException;
import org.soulwing.snmp.Varbind;
import org.soulwing.snmp.VarbindCollection;
import scanner.DeviceProperties;
import scanner.SNMPBrowser;
import ui.buttons.SlideButton;
import ui.buttons.TextButton;
import ui.inputField.NumberField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {
    public HBox buttonContainer;
    public HBox textButtons;
    public HBox addressContainer;
    public HBox subnetContainer;
    public HBox scanHBox;
    public HBox customOIDBox;
    public VBox root;
    public VBox subnetParent;
    public VBox menuVBox;
    public Button backButton;
    public TextField communityField;
    public TextField customOIDInput;
    public ListView<Label> deviceList;
    public TableView<Pair<String, String>> propertyTable;

    private boolean scanNetwork = true;
    private final ArrayList<DeviceProperties> devices = new ArrayList<>();
    private String currentDisplayedDevice = "";

    public void initialize() throws IOException {
        this.addInitialSceneElements();
        SNMPBrowser.initialize();
        SNMPBrowser.startTrapListener();
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

    private void addInitialSceneElements() {
        this.addDarkModeButton();
        this.addTextButtons();
        this.addAddressFields();
        this.setDeviceListListener();
        this.initializePropertyTable();
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
        this.buttonContainer.getChildren().add(btn);
    }

    private void addTextButtons() {
        String[] captions = {"Scan network", "Scan device"};
        for (AtomicInteger i = new AtomicInteger(0); i.get() < captions.length; i.set(i.get() + 1)) {
            TextButton btn = new TextButton(captions[i.get()], 300, 75);
            btn.setOnMouseClicked(e -> {
                this.scanNetwork = (i.get() == 0);
                this.subnetParent.setVisible(this.scanNetwork);
                for (Node t : this.textButtons.getChildren()) {
                    ((TextButton)t).clear();
                }
                btn.highlight();
            });
            if (i.get() == 0) {
                btn.highlight();
            }
            this.textButtons.getChildren().add(btn);
        }
    }

    private void changeTheme(boolean isDarkMode) {
        String path = "file:src/resources/" + (isDarkMode ? "dark" : "light") + "Mode.css";
        Main.getScene().getStylesheets().set(0, path);
    }

    private void changeScene() {
        this.menuVBox.setVisible(!this.menuVBox.isVisible());
        this.scanHBox.setVisible(!this.scanHBox.isVisible());
        this.backButton.setVisible(this.scanHBox.isVisible());
        this.customOIDBox.setVisible(this.scanHBox.isVisible());
    }

    private void setDeviceListListener() {
        this.deviceList.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends Label> ov, Label oldVal, Label newVal) -> {
                    this.currentDisplayedDevice = newVal.getText();
                    this.updatePropertyTable();
                    if (newVal != null) {
                        newVal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold");
                    }
                    if (oldVal != null) {
                        oldVal.setStyle("-fx-font-size: 16px");
                    }
                }
        );
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

    private String getIPFromScene() {
        String[] ipParts = new String[7];
        for (int i = 0; i < ipParts.length; i++) {
            Node node = this.addressContainer.getChildren().get(i);
            String content = node instanceof Label ? ((Label) node).getText() : ((NumberField) node).getText();
            if (content.equals("")) {
                return null;
            }
            ipParts[i] = content;
        }
        return String.join("", ipParts);
    }

    public void sendCustomRequest() {
        String request = this.customOIDInput.getText();
        String community = this.communityField.getText();
        String targetIP = "";
        for (Pair<String, String> p : this.propertyTable.getItems()) {
            if (p.getKey().equals("ipAdEntAddr")) {
                targetIP = p.getValue();
                break;
            }
        }
        SNMPBrowser.sendAsyncSNMPRequest(targetIP, community, request);
    }

    public void backButtonPressed() {
        this.changeScene();
    }

    public void startSNMPProcess() {
        SNMPBrowser.onResponse(snmpEvent -> {
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
                    for (int i = 0; i < this.deviceList.getItems().size(); i++) {
                        if (this.deviceList.getItems().get(i).getText().equals(ip)) {
                            this.devices.get(i).getProperties().putAll(device.getProperties());
                            if (this.currentDisplayedDevice.equals(ip)) {
                                this.updatePropertyTable();
                            }
                            return;
                        }
                    }
                    Label label = new Label(ip);
                    label.setStyle("-fx-font-size: 16px");
                    this.deviceList.getItems().add(label);
                    this.devices.add(device);
                });
            } catch (SnmpException ignore) {
            } finally {
                snmpEvent.getContext().close();
            }
        });


        String ip = this.getIPFromScene();
        String community = this.communityField.getText();
        String netmask = ((NumberField)this.subnetContainer.getChildren().get(1)).getText();
        this.devices.clear();
        this.deviceList.getItems().clear();
        if (SNMPBrowser.startScan(ip, community, netmask, this.scanNetwork)) {

         /*   Label l1 = new Label("000.000.000.000");
            l1.setStyle("-fx-font-size: 16px");
            this.deviceList.getItems().add(l1);
            for (int i = 0; i < 20; i++) {
                Label l = new Label(i + "");
                l.setStyle("-fx-font-size: 16px");
                this.deviceList.getItems().add(l);
            }*/
            this.changeScene();
        }

    }

}
