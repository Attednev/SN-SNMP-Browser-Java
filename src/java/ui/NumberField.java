package ui;

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
       // this.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, CornerRadii.EMPTY, Insets.EMPTY)));
        this.setOnKeyTyped(e -> {
            int keyCode = e.getCharacter().charAt(0);
            if (this.getText().length() > maxDigits || (keyCode > 57 && keyCode != 127) ||
                    (keyCode < 48 && keyCode != 8 && keyCode != 9)) {
                int pos = this.getCaretPosition() - 1;
                if (pos >= 0) {
                    this.setText(this.getText().substring(0, pos) + this.getText().substring(pos + 1));
                    this.positionCaret(pos);
                }
            }
        });
    }

}
