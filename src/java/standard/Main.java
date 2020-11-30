package standard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene scene;

    public static Scene getScene() {
        return Main.scene;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("MainScreen.fxml"));
        primaryStage.setTitle("SNMP - Browser");
        primaryStage.setResizable(false);
        scene = new Scene(root);
        scene.getStylesheets().add("standard/lightMode.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
