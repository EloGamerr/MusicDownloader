package fr.lorek.musicdownloader;

import javax.swing.*;
import java.awt.*;

public class View {
    JFilePicker xmlPicker;
    JFilePicker mucisFolderPicker;

    public View(int width, int height) {
        JFrame frame = new JFrame("Music Downloader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);

        Container contentPane = frame.getContentPane();

        JPanel xmlPickerBox = new JPanel();
        xmlPickerBox.setLayout(new BoxLayout(xmlPickerBox, BoxLayout.PAGE_AXIS));
        xmlPicker = new JFilePicker("Sélectionnez un chemin vers une collection xml de rekordbox", "Rechercher...");
        xmlPicker.setMode(JFilePicker.MODE_OPEN);
        xmlPicker.setFileTypeFilter(".xml", "XML");
        xmlPickerBox.add(xmlPicker);
        
        mucisFolderPicker = new JFilePicker("Sélectionnez un dossier de sortie pour les musiques téléchargées", "Rechercher...");
        mucisFolderPicker.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        mucisFolderPicker.setMode(JFilePicker.MODE_OPEN);
        xmlPickerBox.add(mucisFolderPicker);
        contentPane.add(xmlPickerBox);

        frame.setVisible(true);
    }
}
