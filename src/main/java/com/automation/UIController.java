package com.automation;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableColumn;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class UIController {
    FileOrganiser organiser = new FileOrganiser();

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
    private CheckBox othersCheckBox;

    @FXML
    private ListView<String> logListView;
    private ObservableList<String> logData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        extColumn.setCellValueFactory(new PropertyValueFactory<>("extension"));
        folderColumn.setCellValueFactory(new PropertyValueFactory<>("folder"));

        folderColumn.setSortable(true);

        folderColumn.setSortType(TableColumn.SortType.ASCENDING);

        extComboBox.setItems(FXCollections.observableArrayList(
                ".pdf", ".docx", ".xlsx", ".pptx", ".jpg", ".png", ".rar", ".zip", ".exe", ".msi", ".mp3", ".m4a",
                ".wav", ".mp4", ".avi", ".mkv"));

        for (String ext : organiser.extensionMap.keySet()) {
            rulesData.add(new Rule(ext, organiser.extensionMap.get(ext)));
        }

        for (String log : organiser.logs) {
            logData.add(log);
        }

        organiser.logs.clear();

        rulesTable.setItems(rulesData);
        logListView.setItems(logData);

        rulesTable.getSortOrder().add(folderColumn);
        rulesTable.sort();

        logListView.scrollTo(logData.size() - 1);
    }

    @FXML
    private void handleAddRule() {
        String rawExt = extComboBox.getEditor().getText().trim().toLowerCase();
        String folder = newFolderField.getText();

        if (!rawExt.isEmpty() && !folder.isEmpty()) {

            String processedExt = rawExt.startsWith(".") ? rawExt : "." + rawExt;

            boolean exists = rulesData.stream().anyMatch(r -> r.getExtension().equalsIgnoreCase(processedExt));

            if (exists) {
                statusLabelRules.setText("Status: Rule for " + processedExt + " already exists!");
                return;
            }

            rulesData.add(new Rule(processedExt, folder));
            organiser.addRules(processedExt, folder);
            rulesTable.sort();

            // Clear inputs after adding
            extComboBox.getEditor().clear();
            ;
            newFolderField.clear();
            statusLabelRules.setText("Status: Rule has been set!");
        } else {
            statusLabelRules.setText("Status: Select both an extension and a folder!");
        }
    }

    @FXML
    private void handleRemoveRule() {
        Rule selected = rulesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            rulesData.remove(selected);
            organiser.removeRules(selected);
            ;
        }
    }

    @FXML
    private void handleBrowseNewRuleFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File selected = chooser.showDialog(new Stage());
        if (selected != null) {
            String universalPath = selected.getAbsolutePath().replace("\\", "/");
            newFolderField.setText(universalPath);
        }
    }

    public void addLog(String message) {
        String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logData.add("[" + timestamp + "]" + message);

        logListView.scrollTo(logData.size() - 1);
    }

    @FXML
    private void handleClearLogs() {
        logData.clear();
    }

    @FXML
    private void handleClearStoredLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will permanently delete the log history.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            logData.clear();
            organiser.clearLogFile();
        }
    }

    @FXML
    private void handleRun() {
        if (selectedDir != null) {
            statusLabelDashboard.setText("Status: Organising...");

            boolean moveOthers = othersCheckBox.isSelected();
            organiser.organiseDownloads(selectedDir, moveOthers);

            for (String log : organiser.logs) {
                logData.add(log);
                organiser.writeLogToFile(log);
            }

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

    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset to default rules");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will permanently reset the rules to default.");

        if (alert.showAndWait().get() == ButtonType.OK) {
            organiser.resetToDefaults();
            rulesData.clear();
            for (String ext : organiser.extensionMap.keySet()) {
                rulesData.add(new Rule(ext, organiser.extensionMap.get(ext)));
            }
        }

    }

}
