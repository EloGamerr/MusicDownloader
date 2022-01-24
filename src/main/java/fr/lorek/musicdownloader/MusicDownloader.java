package fr.lorek.musicdownloader;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class MusicDownloader {
    private final static String[] AND_FILTERS = {"Lucas & Steve"};

    private final PropertiesManager propertiesManager;
    private String musicURL;

    public MusicDownloader() {
        this.propertiesManager = new PropertiesManager();
        this.musicURL = "https://www.youtube.com/watch?v=gkTb9GP9lVI";
    }

    public String getMusicURL() {
        return musicURL;
    }

    public String getXmlPath() {
        return this.propertiesManager.getXMLPath();
    }

    public String getMusicsFolder() {
        return this.propertiesManager.getMusicsFolder();
    }

    public void setXmlPath(String xmlPath) {
        this.propertiesManager.setXMLPath(xmlPath);
    }

    public void setMusicsFolder(String musicsFolder) {
        this.propertiesManager.setMusicsFolder(musicsFolder);
    }

    public void setMusicURL(String musicURL) {
        this.musicURL = musicURL;
    }

    public void downloadAndImport() {
        System.out.println(this.getXmlPath());
        System.out.println(this.getMusicsFolder());
        System.out.println(this.getMusicURL());

        System.out.println("Démarrage du téléchargement");
        try {
            File musicFile = downloadMusic();
            if (musicFile != null)
                importMusic(musicFile);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    private void installPythonDependencies() throws IOException, InterruptedException {
        System.out.println("Installation des dépendances Python");
        Process process = startAndWaitForProcess("pip", "install", "youtube-dl", "moviepy", "youtube_title_parse");
        checkScriptError(process);
    }

    private File downloadMusic() throws IOException, InterruptedException, TagException, CannotWriteException, CannotReadException, InvalidAudioFrameException, ReadOnlyFileException {
        installPythonDependencies();

        System.out.println("Lancement du script Python");
        Process process = startAndWaitForProcess("python3", "pytube\\script.py", musicURL, "raw-musics/");
        checkScriptError(process);

        InputStream stdIn = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdIn);
        BufferedReader br = new BufferedReader(isr);

        String line;

        String musicPath = null;
        String title = null;
        String artist = null;

        while ((line = br.readLine()) != null) {
            if (musicPath == null)
                musicPath = line;
            else if (title == null)
                title = line;
            else if (artist == null)
                artist = line;
        }

        if (musicPath != null && title != null && artist != null) {
            File file = new File(musicPath);
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();

            updateTitleTag(tag, title);
            updateArtistTag(tag, title, artist);

            AudioFileIO.write(audioFile);

            return file;
        }

        return null;
    }

    private void importMusic(File musicFile) throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException, ParserConfigurationException, TransformerException, SAXException {
        AudioFile audioFile = AudioFileIO.read(musicFile);
        Tag tag = audioFile.getTag();

        String title = tag.getFirst(FieldKey.TITLE);
        String artist = tag.getFirst(FieldKey.ARTIST);

        copyToMusicsFolder(musicFile, artist);

        updateXML(musicFile.getAbsolutePath(), title, artist);
    }

    private void copyToMusicsFolder(File musicFile, String artist) throws IOException {
        String artistPath = artist.split(";")[0].trim().replaceAll(" ", "_");
        File copied = new File(this.propertiesManager.getMusicsFolder() + "\\" + artistPath + "\\" + musicFile.getName());
        FileUtils.copyFile(musicFile, copied);
    }

    private void updateXML(String musicPath, String title, String artist) throws TransformerException, IOException, ParserConfigurationException, SAXException {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // use the factory to take an instance of the document builder
        DocumentBuilder db = dbf.newDocumentBuilder();
        // parse using the builder to get the DOM mapping of the
        // XML file
        dom = db.parse(this.propertiesManager.getXMLPath());

        Element doc = dom.getDocumentElement();

        Node collection = doc.getElementsByTagName("COLLECTION").item(0);

        Element track = dom.createElement("TRACK");

        addXMLAttribute(dom, "Artist", artist, track);
        addXMLAttribute(dom, "Name", title, track);
        musicPath = musicPath.replaceAll("\\\\", "/");
        addXMLAttribute(dom, "Location", "file://localhost/" + musicPath, track);

        collection.appendChild(track);

        // Update entries amount
        int entries = getXMLTracksAmount(collection);
        addXMLAttribute(dom, "Entries", String.valueOf(entries), collection);

        Transformer tr = TransformerFactory.newInstance().newTransformer();
        tr.setOutputProperty(OutputKeys.METHOD, "xml");
        tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        // send DOM to file
        tr.transform(new DOMSource(dom),
                new StreamResult(new FileOutputStream(this.propertiesManager.getXMLPath())));
    }

    private int getXMLTracksAmount(Node collection) {
        int entries = 0;
        for (int i = 0; i < collection.getChildNodes().getLength(); ++i) {
            if (collection.getChildNodes().item(i).getNodeName().equalsIgnoreCase("TRACK"))
                ++entries;
        }
        return entries;
    }

    private void addXMLAttribute(Document dom, String attrName, String attrValue, Node element) {
        Attr artistAttr = dom.createAttribute(attrName);
        artistAttr.setNodeValue(attrValue);
        element.getAttributes().setNamedItem(artistAttr);
    }

    private void updateTitleTag(Tag tag, String title) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        title = titleSplit[0].split("\\)")[0].split(" \\(")[0].trim();
        tag.setField(FieldKey.TITLE, title);
    }

    private void updateArtistTag(Tag tag, String title, String artist) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        String[] artistSplit = splitTitleOrArtist(artist);
        StringBuilder artistTag = new StringBuilder(artistSplit[0].split("\\)")[0].split(" \\(")[0].trim());

        for (int i = 1; i < artistSplit.length; ++i) {
            artistTag.append(";").append(artistSplit[i].split("\\)")[0].split(" \\(")[0].trim());
        }

        for (int i = 1; i < titleSplit.length; ++i) {
            artistTag.append(";").append(titleSplit[i].split("\\)")[0].split(" \\(")[0].trim());
        }

        for (String andFilter : AND_FILTERS) {
            if (artistTag.toString().contains(andFilter)) {
                artistTag = new StringBuilder(artistTag.toString().replaceAll(andFilter, andFilter.replaceAll("&", "and")));
            }
        }

        artistTag = new StringBuilder(artistTag.toString().replaceAll(" & ", ";").replaceAll(", ", ";").trim());

        for (String andFilter : AND_FILTERS) {
            andFilter = andFilter.replaceAll("&", "and");
            if (artistTag.toString().contains(andFilter)) {
                artistTag = new StringBuilder(artistTag.toString().replaceAll(andFilter, andFilter.replaceAll("and", "&")));
            }
        }

        tag.setField(FieldKey.ARTIST, artistTag.toString());
    }

    private String[] splitTitleOrArtist(String str) {
        return str.split("\\(?[fF](ea)?t\\.? ");
    }

    private void checkScriptError(Process process) throws IOException {
        String line;

        InputStream stdErr = process.getErrorStream();
        InputStreamReader isrErr = new InputStreamReader(stdErr);
        BufferedReader brErr = new BufferedReader(isrErr);

        while ((line = brErr.readLine()) != null) {
            System.err.println(line);
        }
    }

    private Process startAndWaitForProcess(String... commands) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        Process process = processBuilder.start();
        process.waitFor();
        return process;
    }
}
