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
            if (newValue.length() > maxDigits) {
                this.setText(oldValue);
            } else {
                try {
                    if (!newValue.equals("")) { // Throw an error fo we enter a char which results in the old value
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
