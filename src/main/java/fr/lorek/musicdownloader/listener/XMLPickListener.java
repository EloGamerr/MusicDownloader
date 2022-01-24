package fr.lorek.musicdownloader.listener;

import fr.lorek.musicdownloader.MusicDownloader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class XMLPickListener implements ActionListener {
    private final MusicDownloader musicDownloader;

    public XMLPickListener(MusicDownloader musicDownloader) {
        this.musicDownloader = musicDownloader;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = (JFileChooser) e.getSource();
        if(jFileChooser.getSelectedFile() != null) {
            this.musicDownloader.setXmlPath(jFileChooser.getSelectedFile().getAbsolutePath());
        }
    }
}
