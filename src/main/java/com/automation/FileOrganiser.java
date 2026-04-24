package com.automation;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOrganiser {
    private static final int MAX_LOG_LINES = 1000;

    private final Map<String, String> extensionMap = new HashMap<>();
    private final List<String> logs = new ArrayList<>();
    private final Path appDataPathOverride;

    public FileOrganiser() {
        this(null);
    }

    public FileOrganiser(Path appDataPathOverride) {
        this.appDataPathOverride = appDataPathOverride;
        loadRules();
        readLogFromFile();
    }

    public Map<String, String> getExtensionMap() {
        return extensionMap;
    }

    public List<String> getLogs() {
        return logs;
    }

    public void initialiseDefaultRules() {
        extensionMap.put(".pdf", "Organised/Documents/PDFs");
        extensionMap.put(".docx", "Organised/Documents/Documents");
        extensionMap.put(".pptx", "Organised/Documents/Presentations");
        extensionMap.put(".xlsx", "Organised/Documents/Spreadsheets");
        extensionMap.put(".jpg", "Organised/Images");
        extensionMap.put(".png", "Organised/Images");
        extensionMap.put(".rar", "Organised/Archives");
        extensionMap.put(".zip", "Organised/Archives");
        extensionMap.put(".exe", "Organised/Executables");
        extensionMap.put(".msi", "Organised/Executables");
        extensionMap.put(".mp3", "Organised/Audio");
        extensionMap.put(".m4a", "Organised/Audio");
        extensionMap.put(".wav", "Organised/Audio");
        extensionMap.put(".mp4", "Organised/Video");
        extensionMap.put(".avi", "Organised/Video");
        extensionMap.put(".mkv", "Organised/Video");
    }

    public OperationSummary organiseFile(Path directory, boolean moveOthers) {
        if (directory == null || !Files.isDirectory(directory)) {
            OperationSummary invalidSummary = new OperationSummary();
            invalidSummary.addFailure("Selected path is not a valid folder.");
            String invalidEntry = "[" + timestamp() + "] Selected path is not a valid folder.";
            logs.add(invalidEntry);
            return invalidSummary;
        }

        OperationSummary summary = new OperationSummary();
        String cleanDir = directory.toFile().getAbsolutePath().replace("\\", "/");
        String logEntry = "[" + timestamp() + "] Moving files in " + cleanDir + ".";
        logs.add(logEntry);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                // Skip directories and only process files
                if (Files.isRegularFile(file)) {
                    String logMessage = sortFile(file, moveOthers, summary);
                    logs.add(logMessage);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Could not read folder: " + e.getMessage();
            summary.addFailure(errorMessage);
            logs.add("[" + timestamp() + "] " + errorMessage);
        }

        logEntry = "[" + timestamp() + "] Finished moving files in " + cleanDir + ".";
        logs.add(logEntry);
        return summary;
    }

    private String sortFile(Path source, boolean moveOthers, OperationSummary summary) {
        String fileName = source.getFileName().toString().toLowerCase();
        int lastDotIndex = fileName.lastIndexOf('.');

        String targetFolder = null;
        String logMessage;

        if (lastDotIndex > 0) {
            String extension = fileName.substring(lastDotIndex);
            targetFolder = extensionMap.get(extension);
        }

        if (targetFolder == null && moveOthers) {
            targetFolder = "Organised/Others";
        }

        if (targetFolder != null) {
            try {
                logMessage = moveFile(source, targetFolder);
                summary.incrementMoved();
            } catch (IOException e) {
                logMessage = "Failed to move " + source.getFileName() + ": " + e.getMessage();
                summary.addFailure(logMessage);
            }
        } else {
            logMessage = "Skipped " + fileName + " (no matching rule).";
            summary.incrementSkipped();
        }

        String logEntry = "[" + timestamp() + "] " + logMessage;

        return logEntry;
    }

    private String moveFile(Path source, String folderName) throws IOException {
        Path targetDir = source.getParent().resolve(folderName);

        Files.createDirectories(targetDir);

        Path targetPath = resolveConflictSafePath(targetDir, source.getFileName().toString());
        Files.move(source, targetPath);

        String logMessage = source.getFileName() + " -> " + folderName;
        if (!targetPath.getFileName().equals(source.getFileName())) {
            logMessage += " (renamed to " + targetPath.getFileName() + ")";
        }

        return logMessage;
    }

    private Path resolveConflictSafePath(Path targetDir, String originalName) throws IOException {
        Path targetPath = targetDir.resolve(originalName);
        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        int dotIndex = originalName.lastIndexOf('.');
        String namePart = dotIndex > 0 ? originalName.substring(0, dotIndex) : originalName;
        String extension = dotIndex > 0 ? originalName.substring(dotIndex) : "";

        int attempt = 1;
        while (Files.exists(targetPath)) {
            String candidateName = namePart + " (" + attempt + ")" + extension;
            targetPath = targetDir.resolve(candidateName);
            attempt++;
        }

        return targetPath;
    }

    public void writeLogToFile(String message) {
        try {
            Path folderPath = getAppDataPath();

            Files.createDirectories(folderPath);

            Path logFile = logFilePath();
            String logEntry = message + System.lineSeparator();

            Files.write(
                    logFile,
                    logEntry.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            Path folderPath = getAppDataPath();
            System.err.println("FAILED TO WRITE TO: " + folderPath.toAbsolutePath());
        }
    }

    public void readLogFromFile() {
        try {
            Path logFile = logFilePath();

            if (Files.exists(logFile)) {
                logs.clear();
                List<String> fileLogs = Files.readAllLines(logFile);
                int from = Math.max(0, fileLogs.size() - MAX_LOG_LINES);
                logs.addAll(fileLogs.subList(from, fileLogs.size()));
            }
        } catch (IOException e) {
            System.err.println("Couldn't read logs: " + e.getMessage());
        }
    }

    public void addRules(String ext, String directory) {
        extensionMap.put(ext, directory);
        saveRules();
    }

    public void removeRules(Rule rule) {
        extensionMap.remove(rule.getExtension());
        saveRules();
    }

    public void saveRules() {
        try {
            Path path = getAppDataPath().resolve("rules.csv");
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            List<String> lines = new ArrayList<>();
            extensionMap.forEach((ext, folder) -> lines.add(escapeRuleValue(ext) + "," + escapeRuleValue(folder)));

            Files.write(
                    path,
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save rules: " + e.getMessage());
        }
    }

    public void loadRules() {
        Path path = getAppDataPath().resolve("rules.csv");
        if (Files.exists(path)) {
            try {
                List<String> lines = Files.readAllLines(path);
                extensionMap.clear();
                for (String line : lines) {
                    String[] parts = splitEscapedCsvLine(line);
                    if (parts.length == 2) {
                        extensionMap.put(unescapeRuleValue(parts[0]), unescapeRuleValue(parts[1]));
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to load rules: " + e.getMessage());
            }
        } else {
            initialiseDefaultRules();
        }
    }

    public void resetToDefaults() {
        extensionMap.clear();
        initialiseDefaultRules();
        saveRules();
    }

    public void clearLogFile() {
        try {
            Path logFile = logFilePath();

            if (Files.exists(logFile)) {
                Files.write(logFile, new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Could not clear log file: " + e.getMessage());
        }
    }

    private Path getAppDataPath() {
        if (appDataPathOverride != null) {
            return appDataPathOverride;
        }

        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path path;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData == null || appData.isBlank()) {
                path = Paths.get(userHome, "AppData", "Roaming", "FileOrganiser");
            } else {
                path = Paths.get(appData, "FileOrganiser");
            }
        } else if (os.contains("mac")) {
            path = Paths.get(userHome, "Library", "Application Support", "FileOrganiser");
        } else {
            path = Paths.get(userHome, ".fileorganiser");
        }

        return path;
    }

    private Path logFilePath() {
        return getAppDataPath().resolve("logs.txt");
    }

    private String timestamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss"));
    }

    private String escapeRuleValue(String value) {
        return value.replace("\\", "\\\\").replace(",", "\\,");
    }

    private String unescapeRuleValue(String value) {
        StringBuilder output = new StringBuilder();
        boolean escaping = false;
        for (char current : value.toCharArray()) {
            if (escaping) {
                output.append(current);
                escaping = false;
            } else if (current == '\\') {
                escaping = true;
            } else {
                output.append(current);
            }
        }
        if (escaping) {
            output.append('\\');
        }
        return output.toString();
    }

    private String[] splitEscapedCsvLine(String line) {
        StringBuilder first = new StringBuilder();
        StringBuilder second = new StringBuilder();
        boolean escaping = false;
        boolean secondField = false;

        for (char current : line.toCharArray()) {
            if (escaping) {
                if (secondField) {
                    second.append(current);
                } else {
                    first.append(current);
                }
                escaping = false;
                continue;
            }

            if (current == '\\') {
                escaping = true;
                continue;
            }

            if (current == ',' && !secondField) {
                secondField = true;
                continue;
            }

            if (secondField) {
                second.append(current);
            } else {
                first.append(current);
            }
        }

        if (!secondField) {
            return new String[0];
        }

        return new String[] {first.toString(), second.toString()};
    }

    public static class OperationSummary {
        private int movedCount;
        private int skippedCount;
        private int failedCount;
        private final List<String> failures = new ArrayList<>();

        public int getMovedCount() {
            return movedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public int getFailedCount() {
            return failedCount;
        }

        public List<String> getFailures() {
            return Collections.unmodifiableList(failures);
        }

        private void incrementMoved() {
            movedCount++;
        }

        private void incrementSkipped() {
            skippedCount++;
        }

        private void addFailure(String message) {
            failedCount++;
            failures.add(message);
        }
    }
}
