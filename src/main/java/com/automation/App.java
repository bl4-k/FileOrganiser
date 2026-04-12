package com.automation;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        // Path to the downloads - might expand in future to any selected folder
        Path downloadsPath = Paths.get(System.getProperty("user.home"), "Downloads");

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(downloadsPath)) {
            for (Path entry : stream) {
                System.out.println("Found file: " + entry.getFileName());
            }
        } catch (IOException e) {
            System.err.println("Error reading folder: " + e.getMessage());
        }
    }
}
