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
        String targetFolder = "Others";

        if (fileName.endsWith(".pdf")) {
            targetFolder = "Documents/PDFs";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            targetFolder = "Images";
        } else if (fileName.endsWith(".zip") || fileName.endsWith(".rar")) {
            targetFolder = "Archives";
        }

        moveFile(source, targetFolder);
    }

    private void moveFile(Path source, String folderName) throws IOException {
        Path targetDir = source.getParent().resolve(folderName);

        if (Files.notExists(targetDir)){
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(source.getFileName());
        Files.move(source, targetPath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Moved: " + source.getFileName() + " -> " + folderName);
    }
}
