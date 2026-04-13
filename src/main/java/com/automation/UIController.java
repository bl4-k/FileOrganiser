package com.automation;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UIController {

    @FXML
    private Label statusLabel;

    @FXML
    private void handleRun() {
        statusLabel.setText("Status: Organising...");

        FileOrganiser organiser = new FileOrganiser();
        organiser.organiseDownloads();

        statusLabel.setText("Status: Done! Check your folders.");
    }
    
}
