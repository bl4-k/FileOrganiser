package com.automation;

import java.io.File;
import java.nio.file.Path;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class UIController {

    private Path selectedDir;

    @FXML private Label statusLabel;

    @FXML private Label pathLabel;

    @FXML private TableView<Rule> rulesTable;
    @FXML private TableColumn<Rule, String> extColumn;
    @FXML private TableColumn<Rule, String> folderColumn;

    private ObservableList<Rule> rulesData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        extColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));

        rulesData.add(new Rule(".pdf", "Organised/Documents/PDFs"));
        rulesData.add(new Rule(".png", "Organised/Images"));

        rulesTable.setItems(rulesData);
    }

    @FXML
    private void handleAddRule() {
        rulesData.add(new Rule(".ext", "TargetFolder"));
    }

    @FXML
    private void handleRemoveRule() {
        Rule selected = rulesTable.getSelectionModel().getSelectedItem();
        if (selected != null){
            rulesData.remove(selected);
        }
    }
 
    @FXML
    private void handleRun() {
        if (selectedDir != null) {
            statusLabel.setText("Status: Organising...");

            FileOrganiser organiser = new FileOrganiser();

            //Clearing old rules and setting the new ones
            organiser.extensionMap.clear();
            for (Rule r : rulesData) {
                organiser.extensionMap.put(r.getExtension(), r.getFolder());
            }

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
