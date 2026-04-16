package com.automation;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileOrganiser {

    Map<String, String> extensionMap = new HashMap<>();
    ArrayList<String> logs = new ArrayList<>();

    public FileOrganiser() {
        initialiseDefaultRules();
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
        logs.add("Moving files in " + directory.toFile().getAbsolutePath().replace("\\", "/") + ".");

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

        logs.add("Finished moving files in " + directory.toFile().getAbsolutePath().replace("\\", "/") + ".");
    }

    private String sortFile(Path source, boolean moveOthers) throws IOException {
        String fileName = source.getFileName().toString().toLowerCase();
        int lastDotIndex = fileName.lastIndexOf('.');

        String targetFolder = null;

        if (lastDotIndex > 0) {
            String extension = fileName.substring(lastDotIndex);
            targetFolder = extensionMap.get(extension);
        }

        if (targetFolder == null && moveOthers) {
            targetFolder = "Organised/Others";
        }

        if (targetFolder != null) {
            return moveFile(source, targetFolder);
        }

        return "Unable to move " + fileName + ".";
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
            File logFile = new File("organiser_logs.txt");
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String logEntry = "[" + timestamp + "] " + message + System.lineSeparator();

            Files.write(logFile.toPath(), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }   catch (IOException e) {
            System.err.println("Could not write to log file: " + e.getMessage());
        }
    }

}
