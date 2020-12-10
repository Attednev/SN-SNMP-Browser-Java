package ui.buttons;

import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SlideButton extends StackPane {
    private boolean on = true;
    private Circle slider;
    private Rectangle background;

    public SlideButton() {
        Platform.runLater(this::addInitialItems);
    }

    private void addInitialItems() {
        double width = this.getPrefWidth();
        double height = this.getPrefHeight();
        Rectangle foundationBorder = new Rectangle(width, height);
        foundationBorder.setOnMouseClicked(e -> update());
        foundationBorder.setFill(
                new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(205, 205, 205)),
                        new Stop(1, Color.rgb(111, 111, 111)))
        );
        foundationBorder.setArcHeight(width);
        foundationBorder.setArcWidth(height);

        this.background = new Rectangle(width / 5 * 3, height / 5 * 3);
        this.background.setOnMouseClicked(e -> update());
        this.background.setFill(Color.rgb(91, 91, 91));
        this.background.setArcHeight(width / 2);
        this.background.setArcWidth(height / 2);

        this.slider = new Circle(height / 9 * 4);
        this.slider.setOnMouseClicked(e -> this.update());
        this.slider.setTranslateX(-(width / 4));
        this.slider.setFill(
                new LinearGradient(0, 1, 0, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(160, 160, 160)),
                        new Stop(0.5, Color.rgb(188, 188, 188)),
                        new Stop(1, Color.rgb(213, 213, 213)))
        );
        this.getChildren().addAll(foundationBorder, this.background, slider);
        if (this.on) {
            this.on = false;
            this.update();
        }
    }

    private void update() {
        this.on = !this.on;
        double width = this.getPrefWidth();
        double diff = this.on ? width / 4 : -(width / 4);

        TranslateTransition tt = new TranslateTransition(Duration.millis(150), this.slider);
        tt.setByX(2 * diff);

        FillTransition ft = new FillTransition(Duration.millis(150), this.background);
        ft.setFromValue(this.on ? Color.rgb(91, 91, 91) : Color.rgb(255, 165, 0));
        ft.setToValue(this.on ?  Color.rgb(255, 165, 0) : Color.rgb(91, 91, 91));
        ParallelTransition pt = new ParallelTransition(tt, ft);
        pt.play();
    }


}
