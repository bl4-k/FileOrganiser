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

        stage.setTitle("FileOrganiser");
        stage.setScene(new Scene(root, 600, 500));
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
