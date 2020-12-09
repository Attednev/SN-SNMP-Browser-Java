package ui.inputField;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

public class NumberField extends TextField {
    private final IntegerProperty maxDigits = new SimpleIntegerProperty();

    public NumberField() {
        setupListener();
    }

    public NumberField(int maxDigits) {
        this.maxDigits.setValue(maxDigits);
    }

    private void setupListener() {
        this.setFont(new Font("Arial", 25));
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > this.maxDigits.get()) {
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

    public final void setMaxDigits(int maxDigits) {
        this.maxDigits.set(maxDigits);
    }

    public final int getMaxDigits() {
        return this.maxDigits.get();
    }

}
