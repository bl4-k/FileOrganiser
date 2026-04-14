package com.automation;

public class Rule {
    private String extension;
    private String folder;

    public Rule(String extension, String folder) {
        this.extension = extension;
        this.folder = folder;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String ext) {
        this.extension = ext;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}