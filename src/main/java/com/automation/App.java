package com.automation;

public class App {
    public static void main(String[] args) {
       System.out.println("Automation");
       FileOrganiser organiser = new FileOrganiser();
       organiser.organiseDownloads();
       System.out.println("Done.");
    }
}
