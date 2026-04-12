package com.automation;

import java.io.IOException;
import java.nio.file.*;

public class FileOrganiser {
    public void organiseDownloads() {
        Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadsDir)) {
            for (Path file : stream) {
                // Skip directories and only process files
                if (Files.isRegularFile(file)) {
                    sortFile(file);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading Downloads folder: " + e.getMessage());
        }

    }

    private void sortFile(Path source) throws IOException {
        String fileName = source.getFileName().toString().toLowerCase();
        String targetFolder = "Organised/Others";

        if (fileName.endsWith(".pdf")) {
            targetFolder = "Organised/Documents/PDFs";
        } else if (fileName.endsWith(".pptx")) {
            targetFolder = "Organised/Documents/Presentations";
        } else if (fileName.endsWith(".docx")) {
            targetFolder = "Organised/Documents/Documents";
        } else if (fileName.endsWith(".xlsx")) {
            targetFolder = "Organised/Documents/Spreadsheets";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            targetFolder = "Organised/Images";
        } else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            targetFolder = "Organised/Archives";
        } else if (fileName.endsWith(".exe") || fileName.endsWith(".msi")) {
            targetFolder = "Organised/Others/Executables";
        }

        moveFile(source, targetFolder);
    }

    private void moveFile(Path source, String folderName) throws IOException {
        Path targetDir = source.getParent().resolve(folderName);

        if (Files.notExists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(source.getFileName());
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Moved: " + source.getFileName() + " -> " + folderName);
    }
}
