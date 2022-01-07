package fr.lorek.musicdownloader;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new View(500, 500);
            }
        });

        /*MusicDownloader musicDownloader = new MusicDownloader();
        musicDownloader.downloadAndImport();*/
    }
}
