package standard;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class Main extends Application {

    private static Scene scene;

    public static Scene getScene() {
        return Main.scene;
    }

    public static void alertBox(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("SNMP-Trap");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Scene.fxml"));
        Main.scene = new Scene(root, 800, 600);
        Main.scene.getStylesheets().add("file:src/resources/darkMode.css");
        primaryStage.setScene(scene);
        primaryStage.setTitle("SNMP - Browser");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
