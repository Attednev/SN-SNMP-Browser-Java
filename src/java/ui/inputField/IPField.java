package ui.inputField;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;

public class IPField extends HBox {
    private final BooleanProperty hasMask = new SimpleBooleanProperty();

    public IPField() {
        this.createChildren();
        this.setSpacing(5);
    }

    private void createChildren() {
        for (int i = 0; i < 4; i++) {
            this.getChildren().add(this.createNumberField(3, 100, 40));
            if (i < 3) {
                this.getChildren().add(new Label("."));
            }
        }
        this.getChildren().add(new Label("/"));
        this.getChildren().add(this.createNumberField(2, 66.66, 40));
    }

    private NumberField createNumberField(int digits, double width, double height) {
        NumberField nf = new NumberField(digits);
        nf.setMinSize(width, height);
        nf.setMaxSize(width, height);
        nf.setFont(new Font("Arial", 25));
        return nf;
    }

    public void setHasMask(boolean hasMask) {
        this.hasMask.setValue(hasMask);
        this.getChildren().get(7).setVisible(hasMask);
        this.getChildren().get(8).setVisible(hasMask);
    }

    public boolean getHasMask() {
        return this.hasMask.getValue();
    }

    public String getMask() {
        return ((NumberField)this.getChildren().get(8)).getText();
    }

    public String getIP() {
        StringBuilder ip = new StringBuilder();
        for (Node n : this.getChildren()) {
            if (n instanceof NumberField) {
                ip.append(((NumberField) n).getText());
            } else {
                ip.append(".");
            }
        }
        return ip.toString();
    }

}
