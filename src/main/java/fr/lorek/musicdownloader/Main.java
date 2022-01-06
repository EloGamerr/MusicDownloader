package fr.lorek.musicdownloader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String home = System.getProperty("user.home");
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("python3", "pytube\\script.py", "https://www.youtube.com/watch?v=dzHdo4yxidc", home + "\\Downloads");
        try {
            Process process = processBuilder.start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
