package ui.buttons;

import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

@DefaultProperty("text")
public class TextButton extends StackPane {
    private final StringProperty text = new SimpleStringProperty();
    private final BooleanProperty isHighlighted = new SimpleBooleanProperty(false);

    public TextButton() {}


    private void update() {
        Label label = new Label(this.text.getValue());
        label.setFont(new Font("Arial", 35));
        this.getChildren().add(label);
        if (this.isHighlighted.get()) {
            this.getStyleClass().add("highlight");
        } else {
            this.getStyleClass().clear();
        }
    }

    public void setText(String text) {
        this.text.set(text);
        this.update();
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.isHighlighted.set(isHighlighted);
        this.update();
    }

    public String getText() {
        return this.text.getValue();
    }

    public boolean getIsHighlighted() {
        return this.isHighlighted.get();
    }

}
