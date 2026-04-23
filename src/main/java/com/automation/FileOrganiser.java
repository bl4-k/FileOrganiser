package com.automation;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileOrganiser {

    private final Map<String, String> extensionMap = new HashMap<>();
    private final List<String> logs = new ArrayList<>();

    public FileOrganiser() {
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

    public void organiseFile(Path directory, boolean moveOthers) {
        String cleanDir = directory.toFile().getAbsolutePath().replace("\\", "/");
        String logEntry = "[" + timestamp() + "] Moving files in " + cleanDir + ".";
        logs.add(logEntry);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path file : stream) {
                // Skip directories and only process files
                if (Files.isRegularFile(file)) {
                    String logMessage = sortFile(file, moveOthers);
                    logs.add(logMessage);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Downloads folder: " + e.getMessage());
        }

        logEntry = "[" + timestamp() + "] Finished moving files in " + cleanDir + ".";
        logs.add(logEntry);
    }

    private String sortFile(Path source, boolean moveOthers) throws IOException {
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
            logMessage = moveFile(source, targetFolder);
        } else {
            logMessage = "Unable to move " + fileName + ".";
        }

        String logEntry = "[" + timestamp() + "] " + logMessage;

        return logEntry;
    }

    private String moveFile(Path source, String folderName) throws IOException {
        Path targetDir = source.getParent().resolve(folderName);

        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(source.getFileName());
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);

        String logMessage = source.getFileName() + " -> " + folderName;

        return logMessage;
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
                logs.addAll(Files.readAllLines(logFile));
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
            extensionMap.forEach((ext, folder) -> lines.add(ext + "," + folder));

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
                    String[] parts = line.split(",", 2);
                    if (parts.length == 2) {
                        addRules(parts[0], parts[1]);
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
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path path;

        if (os.contains("win")) {
            path = Paths.get(System.getenv("APPDATA"), "FileOrganiser");
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
}
