package com.automation;

import java.io.File;
import java.nio.file.Path;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class UIController {

    private Path selectedDir;

    @FXML
    private Label statusLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private void handleRun() {
        if (selectedDir != null) {
            statusLabel.setText("Status: Organising...");

            FileOrganiser organiser = new FileOrganiser();
            organiser.organiseDownloads(selectedDir);

            statusLabel.setText("Status: Done! Check your folders.");
        }
    }

    @FXML
    private void handleSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Organise");

        File selectedFile = directoryChooser.showDialog(new Stage());

        if (selectedFile != null) {
            selectedDir = selectedFile.toPath();
            pathLabel.setText(selectedDir.toString());
        }
    }

}
