package fr.lorek.musicdownloader;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MusicDownloader musicDownloader = new MusicDownloader();

        SwingUtilities.invokeLater(() -> new View(500, 300, musicDownloader));
    }
}
