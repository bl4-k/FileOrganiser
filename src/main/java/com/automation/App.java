package com.automation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        var url = getClass().getResource("/MainView.fxml");
        if (url == null) {
            throw new RuntimeException("Cannot find MainView.fxml");
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();

        Scene scene = new Scene(root, 760, 560);
        var stylesheet = getClass().getResource("/app.css");
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }

        stage.setTitle("FileOrganiser");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setMinWidth(700);
        stage.setMinHeight(520);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
