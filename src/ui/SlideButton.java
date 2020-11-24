package ui;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SlideButton extends StackPane {
    private boolean on = false;
    private final double width;
    private final double height;
    private Circle slider;
    private Rectangle background;
    private Runnable action;

    public SlideButton(double width, double height) {
        this.width = width;
        this.height = height;
        this.setWidth(width);
        this.setHeight(height);
        this.addInitialItems();

    }

    private void addInitialItems() {
        Rectangle foundationBorder = new Rectangle(this.width, this.height);
        foundationBorder.setOnMouseClicked(e -> {
            update();
        });
        foundationBorder.setFill(
                new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(205, 205, 205)),
                        new Stop(1, Color.rgb(111, 111, 111)))
        );
        foundationBorder.setArcHeight(this.width);
        foundationBorder.setArcWidth(this.height);


        this.background = new Rectangle(this.width / 5 * 3, this.height / 5 * 3);
        this.background.setOnMouseClicked(e -> {
            update();
        });
        this.background.setFill(Color.rgb(91, 91, 91));
        this.background.setArcHeight(this.width / 2);
        this.background.setArcWidth(this.height / 2);


        this.slider = new Circle(this.height / 9 * 4);
        this.slider.setOnMouseClicked(e -> {
            this.update();
        });
        this.slider.setTranslateX(-(this.width / 4));
        this.slider.setFill(
                new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(160, 160, 160)),
                        new Stop(0.5, Color.rgb(188, 188, 188)),
                        new Stop(1, Color.rgb(213, 213, 213)))
        );
        this.getChildren().addAll(foundationBorder, this.background, slider);

    }

    private void update() {
        this.on = !this.on;
        double diff = this.on ? this.width / 4 : -(this.width / 4);
        TranslateTransition tt = new TranslateTransition(Duration.millis(150), this.slider);
        tt.setByX(2 * diff);

        FillTransition ft = new FillTransition(Duration.millis(150), this.background);
        ft.setFromValue(this.on ? Color.rgb(91, 91, 91) : Color.rgb(255, 165, 0));
        ft.setToValue(this.on ?  Color.rgb(255, 165, 0) : Color.rgb(91, 91, 91));
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
        action.run();
    }

    public void onAction(Runnable action) {
        this.action = action;
    }

    public boolean isOn() {
        return this.on;
    }

}
