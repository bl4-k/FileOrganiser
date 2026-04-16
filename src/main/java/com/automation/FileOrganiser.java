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

    Map<String, String> extensionMap = new HashMap<>();
    ArrayList<String> logs = new ArrayList<>();

    public FileOrganiser() {
        loadRules();
        readLogFromFile();
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
    }

    public void organiseDownloads(Path directory, boolean moveOthers) {
        String cleanDir = directory.toFile().getAbsolutePath().replace("\\", "/");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss"));
        String logEntry = "[" + timestamp + "] Moving files in " + cleanDir + ".";
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

        timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss"));
        logEntry = "[" + timestamp + "] Finished moving files in " + cleanDir + ".";
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

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + logMessage;

        return logEntry;
    }

    private String moveFile(Path source, String folderName) throws IOException {
        Path targetDir = source.getParent().resolve(folderName);

        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(source.getFileName());
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);

        String logMessage = source.getFileName() + " -> " + folderName;

        return logMessage;
    }

    public void writeLogToFile(String message) {
        try {
            Path folderPath = getAppDataPath();

            if (Files.notExists(folderPath)) {
                Files.createDirectories(folderPath);
            }

            Path logFile = folderPath.resolve("logs.txt");
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
            logs = (ArrayList<String>) Files.readAllLines(getAppDataPath().resolve("logs.txt"));
        } catch (Exception e) {
            System.err.println("Couldn't read logs.");
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
        } catch (Exception e) {
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
                        extensionMap.put(parts[0], parts[1]);
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load rules: " + e.getMessage());
            }
        } else {
            initialiseDefaultRules();
        }
    }

    public void addRules(String ext, String directory){
        extensionMap.put(ext, directory);
        saveRules();
    }

    public void removeRules(Rule rule){
        extensionMap.remove(rule.getExtension());
        saveRules();
    }



}
