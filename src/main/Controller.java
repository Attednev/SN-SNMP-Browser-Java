package main;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ui.SlideButton;

public class Controller {
    @FXML
    private HBox root;
    @FXML
    private VBox buttonContainer;
    @FXML
    private VBox container;

    @FXML
    private void initialize() {
        double buttonWidth = 90;
        double buttonHeight = 35;
        SlideButton btn = new SlideButton(buttonWidth, buttonHeight);

        btn.onAction(() -> {
            if (btn.isOn()) {
                root.setStyle("-fx-background-color: rgb(80, 80, 80)");
            } else {
                root.setStyle("-fx-background-color: lightgray");
            }
        });
        buttonContainer.getChildren().add(btn);

        container.setPrefSize(root.getPrefWidth() - (buttonWidth + 5) * 2, root.getPrefHeight() - (buttonHeight + 5) * 2);
    }

}
