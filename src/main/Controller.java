package main;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.NumberField;
import ui.SlideButton;
import ui.TextButton;

import java.util.ArrayList;

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
    @FXML
    private Label addressLabel;
    @FXML
    private Label subnetLabel;
    @FXML
    private Button startButton;

    private final ArrayList<Label> menuLabels = new ArrayList<>();
    private boolean scanNetwork = true;

    @FXML
    private void initialize() {
        addDarkModeButton();
        addTextButtons();
        addAddressFields();
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
        label.setStyle("-fx-min-height: 40; -fx-font-size: 30; -fx-text-fill: black");
        this.menuLabels.add(label);
        return label;
    }

    private void addDarkModeButton() {
        double buttonWidth = 90;
        double buttonHeight = 35;
        SlideButton btn = new SlideButton(buttonWidth, buttonHeight);
        btn.onAction(() -> {
            if (btn.isOn()) {
                enableDarkMode();
            } else {
                enableLightMode();
            }
        });
        buttonContainer.getChildren().add(btn);
        textButtons.setPrefSize(root.getPrefWidth() - (buttonWidth + 5) * 2, root.getPrefHeight() - (buttonHeight + 5) * 2);
    }

    private void enableDarkMode() {
        root.setStyle("-fx-background-color: rgb(50, 50, 50)");
        addressLabel.setStyle("-fx-text-fill: white; -fx-font-size: 25");
        subnetLabel.setStyle("-fx-text-fill: white; -fx-font-size: 25");
        startButton.setStyle("-fx-font-size: 20; -fx-background-color: transparent; -fx-border-color: white; -fx-text-fill: white; -fx-cursor: HAND");
        for (Node n : addressContainer.getChildren()) {
            if (n instanceof NumberField) {
                ((NumberField)n).setDarkMode();
            }
        }
        for (Node n : textButtons.getChildren()) {
            if (n instanceof TextButton) {
                ((TextButton)n).setDarkMode();
            }
        }
        for (Node n : subnetContainer.getChildren()) {
            if (n instanceof NumberField) {
                ((NumberField)n).setDarkMode();
            }
        }
        for (Label l : this.menuLabels) {
            l.setStyle("-fx-min-height: 40; -fx-font-size: 30; -fx-text-fill: white");
        }
    }

    private void enableLightMode() {
        root.setStyle("-fx-background-color: lightgray");
        addressLabel.setStyle("-fx-text-fill: black; -fx-font-size: 25");
        subnetLabel.setStyle("-fx-text-fill: black; -fx-font-size: 25");
        startButton.setStyle("-fx-font-size: 20; -fx-background-color: transparent; -fx-border-color: black; -fx-text-fill: black; -fx-cursor: HAND");
        for (Node n : addressContainer.getChildren()) {
            if (n instanceof NumberField) {
                ((NumberField)n).setLightMode();
            }
        }
        for (Node n : textButtons.getChildren()) {
            if (n instanceof TextButton) {
                ((TextButton)n).setLightMode();
            }
        }
        for (Node n : subnetContainer.getChildren()) {
            if (n instanceof NumberField) {
                ((NumberField)n).setLightMode();
            }
        }
        for (Label l : this.menuLabels) {
            l.setStyle("-fx-min-height: 40; -fx-font-size: 30; -fx-text-fill: black");
        }
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

    @FXML
    private void startSNMPProcess() {
        System.out.println("Start process");
    }

}
