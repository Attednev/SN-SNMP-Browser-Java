package ui.inputField;

import javafx.scene.control.TextField;
import javafx.scene.text.Font;

public class NumberField extends TextField {

    public NumberField(int maxDigits) {
        super();
        this.setFont(new Font("Arial", 25));
        this.setMinWidth(90.0 / 2.7 * maxDigits);
        this.setMinHeight(40);
        this.setMaxWidth(82.0 / 2.7 * maxDigits);
        this.setMaxHeight(40);
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (this.getText().length() > maxDigits) {
                this.setText(this.getText().substring(0, maxDigits));
            } else {
                try {
                    if (!this.getText().equals("")) {
                        Integer.parseInt(this.getText());
                    }
                    this.setText(newValue);
                } catch (NumberFormatException | StackOverflowError f) {
                    this.setText(oldValue);
                }
            }
        });
    }

}
