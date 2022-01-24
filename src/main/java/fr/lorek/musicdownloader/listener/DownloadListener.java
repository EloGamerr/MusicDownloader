package fr.lorek.musicdownloader.listener;

import fr.lorek.musicdownloader.MusicDownloader;
import fr.lorek.musicdownloader.View;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DownloadListener implements ActionListener {
    private final View view;
    private final MusicDownloader musicDownloader;

    public DownloadListener(View view, MusicDownloader musicDownloader) {
        this.view = view;
        this.musicDownloader = musicDownloader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String musicURL = this.view.getMusicURL();
        this.musicDownloader.setMusicURL(musicURL);
        this.musicDownloader.downloadAndImport();
    }
}
