package com.automation;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class FileOrganiser {

    Map<String,String> extensionMap = new HashMap<>();

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

        targetFolder = extensionMap.get(fileName.split(".")[-1]);

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
