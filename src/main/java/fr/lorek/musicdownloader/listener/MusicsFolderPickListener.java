package fr.lorek.musicdownloader.listener;

import fr.lorek.musicdownloader.MusicDownloader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MusicsFolderPickListener implements ActionListener {
    private final MusicDownloader musicDownloader;

    public MusicsFolderPickListener(MusicDownloader musicDownloader) {
        this.musicDownloader = musicDownloader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = (JFileChooser) e.getSource();
        if(jFileChooser.getSelectedFile() != null) {
            this.musicDownloader.setMusicsFolder(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }
}
