package ui.buttons;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class TextButton extends StackPane {

    public TextButton(String text, double width, double height) {
        super();
        Label label = new Label(text);
        label.setFont(new Font("Arial", 35));
        this.getChildren().add(label);
        this.setMinWidth(width);
        this.setMinHeight(height);
        this.setMaxWidth(width);
        this.setMaxHeight(height);
    }

    public void highlight() {
        this.getStyleClass().add("highlight");
    }

    public void clear() {
        this.getStyleClass().clear();
    }

}
