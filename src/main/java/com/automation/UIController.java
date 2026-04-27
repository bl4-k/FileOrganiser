package com.automation;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.concurrent.Task;
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
    @FXML
    private ProgressIndicator runProgressIndicator;
    @FXML
    private Button startBtn;
    @FXML
    private Button addRuleBtn;
    @FXML
    private Button removeRuleBtn;
    @FXML
    private Button resetRulesBtn;
    @FXML
    private Button browseRuleFolderBtn;
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

        loadRules();
        loadLogs();

        rulesTable.setItems(rulesData);
        logListView.setItems(logData);
        rulesTable.setPlaceholder(new Label("No custom rules configured."));
        logListView.setPlaceholder(new Label("No logs yet. Run the organiser to generate activity."));
        runProgressIndicator.setVisible(false);

        rulesTable.getSortOrder().add(folderColumn);
        rulesTable.sort();

        if (!logData.isEmpty()) {
            logListView.scrollTo(logData.size() - 1);
        }

        startBtn.setDisable(true);
    }

    @FXML
    private void handleAddRule() {
        String rawExt = extComboBox.getEditor().getText().trim().toLowerCase();
        String folder = newFolderField.getText().trim();

        if (rawExt.isEmpty() || folder.isEmpty()) {
            setStatusRules("Select both an extension and a folder.", StatusType.ERROR);
            return;
        }

        Path folderPath = Paths.get(folder);
        if (!folderPath.toFile().exists() || !folderPath.toFile().isDirectory()) {
            setStatusRules("Select an existing folder path.", StatusType.ERROR);
            return;
        }

        String processedExt = rawExt.startsWith(".") ? rawExt : "." + rawExt;

        // Prevent duplicate extension rules (case-insensitive match)
        boolean exists = rulesData.stream().anyMatch(r -> r.getExtension().equalsIgnoreCase(processedExt));

        if (exists) {
            setStatusRules("Rule for " + processedExt + " already exists.", StatusType.WARNING);
            return;
        }

        rulesData.add(new Rule(processedExt, folder));
        organiser.addRules(processedExt, folder);
        rulesTable.sort();

        extComboBox.getEditor().clear();
        newFolderField.clear();
        newFolderField.requestFocus();
        setStatusRules("Rule saved successfully.", StatusType.SUCCESS);

    }

    @FXML
    private void handleRemoveRule() {
        Rule selected = rulesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatusRules("Select a rule to remove.", StatusType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Rule");
        alert.setHeaderText("Remove selected rule?");
        alert.setContentText("This action updates your saved rule settings.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            rulesData.remove(selected);
            organiser.removeRules(selected);
            setStatusRules("Rule removed.", StatusType.SUCCESS);
        }
    }

    @FXML
    private void handleBrowseNewRuleFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        File selected = chooser.showDialog(rulesTable.getScene().getWindow());
        if (selected != null) {
            String universalPath = selected.getAbsolutePath().replace("\\", "/");
            newFolderField.setText(universalPath);
            setStatusRules("Folder selected.", StatusType.READY);
        }
    }

    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset to default rules");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will permanently reset the rules to default.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            organiser.resetToDefaults();
            loadRules();
            setStatusRules("Default rules restored.", StatusType.SUCCESS);
        }

    }

    // Loads rules from organiser into UI state
    private void loadRules() {
        rulesData.clear();
        for (String ext : organiser.getExtensionMap().keySet()) {
            rulesData.add(new Rule(ext, organiser.getExtensionMap().get(ext)));
        }
    }

    @FXML
    private void handleClearLogs() {
        logData.clear();
        setStatusDashboard("Visible log list cleared.", StatusType.READY);
    }

    @FXML
    private void handleClearStoredLogs() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("This will permanently delete the log history.");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            logData.clear();
            organiser.clearLogFile();
            setStatusDashboard("Saved logs deleted.", StatusType.SUCCESS);
        }
    }

    // Loads logs into UI state
    private void loadLogs() {
        logData.clear();
        logData.addAll(organiser.getLogs());
        organiser.getLogs().clear();
    }

    @FXML
    private void handleRun() {
        if (selectedDir == null) {
            setStatusDashboard("Select a folder before running.", StatusType.ERROR);
            return;
        }

        setRunningState(true);
        setStatusDashboard("Organising files...", StatusType.READY);

        boolean moveOthers = othersCheckBox.isSelected();
        Task<FileOrganiser.OperationSummary> organiseTask = new Task<>() {
            @Override
            protected FileOrganiser.OperationSummary call() {
                return organiser.organiseFile(selectedDir, moveOthers);
            }
        };

        organiseTask.setOnSucceeded(event -> {
            FileOrganiser.OperationSummary summary = organiseTask.getValue();

            for (String log : organiser.getLogs()) {
                logData.add(log);
                organiser.writeLogToFile(log);
            }
            organiser.getLogs().clear();
            if (!logData.isEmpty()) {
                logListView.scrollTo(logData.size() - 1);
            }

            String result = "Moved: " + summary.getMovedCount()
                    + " | Skipped: " + summary.getSkippedCount()
                    + " | Failed: " + summary.getFailedCount();
            StatusType statusType = summary.getFailedCount() > 0 ? StatusType.WARNING : StatusType.SUCCESS;
            setStatusDashboard(result, statusType);
            setRunningState(false);
        });

        organiseTask.setOnFailed(event -> {
            Throwable error = organiseTask.getException();
            String message = (error == null) ? "Unknown error during organise run." : error.getMessage();
            setStatusDashboard("Run failed: " + message, StatusType.ERROR);
            setRunningState(false);
        });

        Thread worker = new Thread(organiseTask, "file-organiser-runner");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void handleSelectFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Organise");

        File selectedFile = directoryChooser.showDialog(new Stage());

        if (selectedFile != null) {
            selectedDir = selectedFile.toPath();
            pathLabel.setText(selectedDir.toString());
            startBtn.setDisable(false);
            setStatusDashboard("Folder selected. Ready to run.", StatusType.READY);
        }
    }

    private void setStatusDashboard(String msg, StatusType type) {
        statusLabelDashboard.setText("Status: " + msg);
        applyStatusStyle(statusLabelDashboard, type);
    }

    private void setStatusRules(String msg, StatusType type) {
        statusLabelRules.setText("Status: " + msg);
        applyStatusStyle(statusLabelRules, type);
    }

    private void applyStatusStyle(Label label, StatusType type) {
        label.getStyleClass().removeAll("status-ready", "status-success", "status-warning", "status-error");
        label.getStyleClass().add(type.styleClass);
    }

    private void setRunningState(boolean running) {
        runProgressIndicator.setVisible(running);
        runProgressIndicator.setManaged(running);
        startBtn.setDisable(running || selectedDir == null);
        setDisableControls(running, addRuleBtn, removeRuleBtn, resetRulesBtn, browseRuleFolderBtn, extComboBox, newFolderField, othersCheckBox);
    }

    private void setDisableControls(boolean disable, Node... nodes) {
        for (Node node : nodes) {
            node.setDisable(disable);
        }
    }

    private enum StatusType {
        READY("status-ready"),
        SUCCESS("status-success"),
        WARNING("status-warning"),
        ERROR("status-error");

        private final String styleClass;

        StatusType(String styleClass) {
            this.styleClass = styleClass;
        }
    }

}
