package fr.lorek.musicdownloader;

import fr.lorek.musicdownloader.listener.DownloadListener;
import fr.lorek.musicdownloader.listener.MusicsFolderPickListener;
import fr.lorek.musicdownloader.listener.XMLPickListener;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class View {
    private final MusicDownloader musicDownloader;
    private JTextField jTextFieldMusicURL;
    private JLabel jLabelDownloadInfos;
    private final JFrame frame;

    public View(int width, int height) {
        this.musicDownloader = new MusicDownloader(this);

        frame = new JFrame("Music Downloader");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new GridLayout(5, 1));

        addXMLPicker(contentPane);
        addMusicsFolderPicker(contentPane);
        addMusicURLTextField(contentPane);
        addDownloadButton(contentPane);
        addDownloadInfosLabel(contentPane);

        frame.setVisible(true);
    }

    public JFrame getFrame() {
        return frame;
    }

    private void addXMLPicker(Container contentPane) {
        JPanel xmlPickerBox = new JPanel();
        xmlPickerBox.setLayout(new GridLayout(1, 1));

        JFilePicker xmlPicker = new JFilePicker("Sélectionnez un chemin vers une collection xml de rekordbox", "Rechercher...");
        xmlPicker.setMode(JFilePicker.MODE_OPEN);
        xmlPicker.setFileTypeFilter(".xml", "XML");
        xmlPicker.getFileChooser().addActionListener(new XMLPickListener(this.musicDownloader));
        xmlPicker.setSelectedFile(new File(this.musicDownloader.getXmlPath()));
        xmlPickerBox.add(xmlPicker);

        contentPane.add(xmlPickerBox);
    }

    private void addMusicsFolderPicker(Container contentPane) {
        JPanel musicsFolderPickerBox = new JPanel();
        musicsFolderPickerBox.setLayout(new GridLayout(1, 1));

        JFilePicker mucisFolderPicker = new JFilePicker("Sélectionnez un dossier de sortie pour les musiques téléchargées", "Rechercher...");
        mucisFolderPicker.getFileChooser().setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        mucisFolderPicker.setMode(JFilePicker.MODE_OPEN);
        mucisFolderPicker.getFileChooser().addActionListener(new MusicsFolderPickListener(this.musicDownloader));
        mucisFolderPicker.setSelectedFile(new File(this.musicDownloader.getMusicsFolder()));
        musicsFolderPickerBox.add(mucisFolderPicker);

        contentPane.add(musicsFolderPickerBox);
    }

    private void addMusicURLTextField(Container contentPane) {
        JPanel textBox = new JPanel();

        JLabel jLabel = new JLabel("URL de la musique à télécharger :");
        textBox.add(jLabel);

        this.jTextFieldMusicURL = new JTextField("", 30);
        textBox.add(this.jTextFieldMusicURL);

        contentPane.add(textBox);
    }

    private void addDownloadButton(Container contentPane) {
        JPanel buttonBox = new JPanel();

        JButton confirmButton = new JButton("Télécharger la musique");
        confirmButton.addActionListener(new DownloadListener(this, this.musicDownloader));
        buttonBox.add(confirmButton);

        contentPane.add(buttonBox);
    }

    private void addDownloadInfosLabel(Container contentPane) {
        JPanel labelBox = new JPanel();

        jLabelDownloadInfos = new JLabel("");
        labelBox.add(jLabelDownloadInfos);

        contentPane.add(labelBox);
    }

    public String getMusicURL() {
        return jTextFieldMusicURL.getText();
    }

    public void displayDownloadInfos(String infos) {
        jLabelDownloadInfos.setText(infos);
    }
}
