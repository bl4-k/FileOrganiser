package com.automation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;

public class AppTest {
    @Test
    public void organiseFileMovesKnownExtensionWithoutOverwrite() throws IOException {
        Path tempRoot = Files.createTempDirectory("fileorganiser-test");
        Path dataRoot = tempRoot.resolve("app-data");
        Path sourceDir = tempRoot.resolve("source");
        Files.createDirectories(sourceDir.resolve("Organised/Documents/PDFs"));

        Path sourceFile = sourceDir.resolve("report.pdf");
        Files.writeString(sourceFile, "new-report");
        Path existingTarget = sourceDir.resolve("Organised/Documents/PDFs/report.pdf");
        Files.writeString(existingTarget, "existing-report");

        FileOrganiser organiser = new FileOrganiser(dataRoot);
        organiser.getExtensionMap().clear();
        organiser.addRules(".pdf", "Organised/Documents/PDFs");

        FileOrganiser.OperationSummary summary = organiser.organiseFile(sourceDir, false);

        Path renamedTarget = sourceDir.resolve("Organised/Documents/PDFs/report (1).pdf");
        assertEquals(1, summary.getMovedCount());
        assertEquals(0, summary.getFailedCount());
        assertFalse(Files.exists(sourceFile));
        assertTrue(Files.exists(existingTarget));
        assertTrue(Files.exists(renamedTarget));
    }

    @Test
    public void organiseFileSkipsUnknownExtensionWhenOthersDisabled() throws IOException {
        Path tempRoot = Files.createTempDirectory("fileorganiser-test");
        Path dataRoot = tempRoot.resolve("app-data");
        Path sourceDir = tempRoot.resolve("source");
        Files.createDirectories(sourceDir);

        Path sourceFile = sourceDir.resolve("archive.unknown");
        Files.writeString(sourceFile, "content");

        FileOrganiser organiser = new FileOrganiser(dataRoot);
        organiser.getExtensionMap().clear();

        FileOrganiser.OperationSummary summary = organiser.organiseFile(sourceDir, false);

        assertEquals(0, summary.getMovedCount());
        assertEquals(1, summary.getSkippedCount());
        assertTrue(Files.exists(sourceFile));
    }

    @Test
    public void saveAndLoadRulesHandlesCommasAndBackslashes() throws IOException {
        Path tempRoot = Files.createTempDirectory("fileorganiser-test");
        Path dataRoot = tempRoot.resolve("app-data");

        FileOrganiser organiser = new FileOrganiser(dataRoot);
        organiser.getExtensionMap().clear();
        organiser.addRules(".csv", "Organised/Reports,Archive");
        organiser.addRules(".txt", "Organised\\TextFiles");

        FileOrganiser reloaded = new FileOrganiser(dataRoot);

        assertEquals("Organised/Reports,Archive", reloaded.getExtensionMap().get(".csv"));
        assertEquals("Organised\\TextFiles", reloaded.getExtensionMap().get(".txt"));
    }
}
