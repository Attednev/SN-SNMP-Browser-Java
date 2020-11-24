package ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class TextButton extends StackPane {
    private final Label label;
    private boolean isDarkMode = false;
    private boolean highlighted = false;

    public TextButton(String text, double width, double height) {
        super();
        this.setMinWidth(width);
        this.setMinHeight(height);
        this.setMaxWidth(width);
        this.setMaxHeight(height);
        this.label = new Label(text);
        this.label.setFont(new Font("Arial", 35));
        this.getChildren().add(this.label);
        this.setLightMode();
        this.clear();
    }

    public void setDarkMode() {
        this.isDarkMode = true;
        this.label.setTextFill(Color.WHITE);
        if (this.highlighted) {
            highlight();
        }
    }

    public void setLightMode() {
        this.isDarkMode = false;
        this.label.setTextFill(Color.BLACK);
        if (this.highlighted) {
            highlight();
        }
    }

    public void highlight() {
        this.setStyle("-fx-border-color: " + ( this.isDarkMode ? "white" : "black") + "; -fx-border-width: 1");
        this.highlighted = true;
    }

    public void clear() {
        this.setStyle("-fx-border-width: 0");
        this.highlighted = false;
    }


}
