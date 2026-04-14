package com.automation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/MainView.fxml"));
        stage.setTitle("Personal Automation Tool");
        stage.setScene(new Scene(root, 600, 500));
        stage.show();
    }
    public static void main(String[] args) {
       launch(args);
    }
}
