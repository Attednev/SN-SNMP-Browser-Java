package main;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import ui.SlideButton;

public class Controller {
    @FXML
    private HBox root;

    @FXML
    private void initialize() {
        System.out.println("Init");
        SlideButton btn = new SlideButton(250, 70);
        btn.onAction(new Runnable() {
            @Override
            public void run() {
                if (btn.isOn()) {
                    root.setStyle("-fx-background-color: rgb(80, 80, 80)");
                } else {
                    root.setStyle("-fx-background-color: lightgray");
                }
            }
        });
        root.getChildren().add(btn);
    }

}
