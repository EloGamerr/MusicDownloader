package fr.lorek.musicdownloader.listener;

import fr.lorek.musicdownloader.MusicDownloader;
import fr.lorek.musicdownloader.View;

import javax.swing.*;
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
        if (musicURL == null || musicURL.isEmpty()) {
            JOptionPane.showMessageDialog(this.view.getFrame(), "Vous devez rentrer une URL de musique youtube !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.musicDownloader.setMusicURL(musicURL);
        JOptionPane.showMessageDialog(this.view.getFrame(), "Téléchargement de la musique en cours...");
        String fileName = this.musicDownloader.downloadAndImport();
        if (fileName == null) {
            JOptionPane.showMessageDialog(this.view.getFrame(), "Une erreur est survenue lors du téléchargement de la musique ! Vérifiez l'URL de la vidéo youtube. Il est aussi possible que la musique soit soumise à une restriction (limite d'âge par exemple).", "Erreur", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this.view.getFrame(), "Musique '" + fileName + "' téléchargée");
        }
    }
}
