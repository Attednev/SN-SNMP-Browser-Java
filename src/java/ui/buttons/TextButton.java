package ui.buttons;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

public class TextButton extends StackPane {
    private StringProperty text = new SimpleStringProperty();

    public TextButton() {
    }

    public TextButton(String text/*String text, double width, double height*/) {
        this.setup(text);
        /*Label label = new Label(text);
        this.getChildren().add(label);
        this.setMinWidth(width);
        this.setMinHeight(height);
        this.setMaxWidth(width);
        this.setMaxHeight(height);*/
    }

    private void setup(String text) {
        Label label = new Label(text);
        label.setFont(new Font("Arial", 35));
        this.getChildren().add(label);
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public String getText() {
        return this.text.getValue();
    }

    public void highlight() {
        this.getStyleClass().add("highlight");
    }

    public void clear() {
        this.getStyleClass().clear();
    }

}
