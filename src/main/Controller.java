package main;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.SlideButton;
import ui.TextButton;

public class Controller {
    @FXML
    private VBox root;
    @FXML
    private HBox buttonContainer;
    @FXML
    private VBox container;

    private boolean scanNetwork = true;

    @FXML
    private void initialize() {
        addDarkModeButton();
        addTextButtons();

    }

    private void addDarkModeButton() {
        double buttonWidth = 90;
        double buttonHeight = 35;
        SlideButton btn = new SlideButton(buttonWidth, buttonHeight);

        btn.onAction(() -> {
            if (btn.isOn()) {
                root.setStyle("-fx-background-color: rgb(50, 50, 50)");
                ((TextButton)container.getChildren().get(0)).setDarkMode();
                ((TextButton)container.getChildren().get(1)).setDarkMode();
            } else {
                root.setStyle("-fx-background-color: lightgray");
                ((TextButton)container.getChildren().get(0)).setLightMode();
                ((TextButton)container.getChildren().get(1)).setLightMode();
            }
        });
        buttonContainer.getChildren().add(btn);
        container.setPrefSize(root.getPrefWidth() - (buttonWidth + 5) * 2, root.getPrefHeight() - (buttonHeight + 5) * 2);
    }

    private void addTextButtons() {
        TextButton btnNetwork = new TextButton("Scan network", 300, 75);
        TextButton btnDevice = new TextButton("Scan device", 300, 75);
        btnNetwork.setOnMouseClicked(e -> {
            this.scanNetwork = true;
            btnNetwork.highlight();
            btnDevice.clear();
        });
        btnDevice.setOnMouseClicked(e -> {
            this.scanNetwork = false;
            btnDevice.highlight();
            btnNetwork.clear();
        });
        btnNetwork.highlight();
        container.getChildren().addAll(btnNetwork, btnDevice);
        container.setPrefWidth(300);
    }

}
