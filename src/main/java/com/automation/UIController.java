package com.automation;

import java.io.File;
import java.nio.file.Path;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class UIController {

    private Path selectedDir;

    @FXML
    private Label statusLabelDashboard;

    @FXML
    private Label pathLabel;

    @FXML
    private TableView<Rule> rulesTable;
    @FXML
    private TableColumn<Rule, String> extColumn;
    @FXML
    private TableColumn<Rule, String> folderColumn;

    private ObservableList<Rule> rulesData = FXCollections.observableArrayList();

    @FXML
    private ComboBox<String> extComboBox;
    @FXML
    private TextField newFolderField;

    @FXML
    private Label statusLabelRules;

    @FXML
    public void initialize() {
        extColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));

        extComboBox.setItems(FXCollections.observableArrayList(
                ".pdf", ".docx", ".xlsx", ".pptx", ".jpg", ".png", ".rar", ".zip", ".exe", ".msi"));

        FileOrganiser organiser = new FileOrganiser();

        for (String ext : organiser.extensionMap.keySet()) {
            rulesData.add(new Rule(ext, organiser.extensionMap.get(ext)));
        }

        rulesTable.setItems(rulesData);
    }

    @FXML
    private void handleAddRule() {
        String ext = extComboBox.getValue();
        String folder = newFolderField.getText();

        if (ext != null && !folder.isEmpty()) {

            boolean exists = rulesData.stream().anyMatch(r -> r.getExtension().equalsIgnoreCase(ext));

            if (exists) {
                statusLabelRules.setText("Status: Rule for " + ext + " already exists!");
                return; // Stop here!
            }

            rulesData.add(new Rule(ext, folder));

            // Clear inputs after adding
            extComboBox.setValue(null);
            newFolderField.clear();
            statusLabelRules.setText("Status: Rule has been set!" );
        } else {
            statusLabelRules.setText("Status: Select both an extension and a folder!");
        }
    }

    @FXML
    private void handleRemoveRule() {
        Rule selected = rulesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rulesData.remove(selected);
        }
    }

    @FXML
    private void handleBrowseNewRuleFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File selected = chooser.showDialog(new Stage());
        if (selected != null) {
            newFolderField.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void handleRun() {
        if (selectedDir != null) {
            statusLabelDashboard.setText("Status: Organising...");

            FileOrganiser organiser = new FileOrganiser();

            // Clearing old rules and setting the new ones
            organiser.extensionMap.clear();
            for (Rule r : rulesData) {
                organiser.extensionMap.put(r.getExtension(), r.getFolder());
            }

            organiser.organiseDownloads(selectedDir);

            statusLabelDashboard.setText("Status: Done! Check your folders.");
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
