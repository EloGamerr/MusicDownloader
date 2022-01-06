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
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class Main {
    private final static String[] andFilters = {"Lucas & Steve"};

    public static void main(String[] args) throws IOException, InterruptedException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException {
        String musicsPath = "musics";
        String musicURL = "https://www.youtube.com/watch?v=WfPu9Jrcpuk";

        Process process = startAndWaitForProcess("pip", "install", "moviepy", "youtube_title_parse");

        checkScriptError(process);

        String home = System.getProperty("user.home");
        process = startAndWaitForProcess("python3", "pytube\\script.py", musicURL, home + "\\Downloads");

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

            String titleTag = updateTitleTag(tag, title);
            String artistTag = updateArtistTag(tag, title, artist);

            AudioFileIO.write(audioFile);

            String artistPath = tag.getFirst(FieldKey.ARTIST).split(";")[0].trim().replaceAll(" ", "_");
            File copied = new File(musicsPath + "\\" + artistPath + "\\" + file.getName());
            FileUtils.copyFile(file, copied);

            updateXML("rekordbox.xml", copied.getAbsolutePath(), titleTag, artistTag);
        }
    }

    private static String updateTitleTag(Tag tag, String title) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        title = titleSplit[0].split("\\)")[0].split(" \\(")[0].trim();
        tag.setField(FieldKey.TITLE, title);
        return title;
    }

    private static String updateArtistTag(Tag tag, String title, String artist) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        String[] artistSplit = splitTitleOrArtist(artist);
        StringBuilder artistTag = new StringBuilder(artistSplit[0].split("\\)")[0].split(" \\(")[0].trim());

        for (int i = 1; i < artistSplit.length; ++i) {
            artistTag.append(";").append(artistSplit[i].split("\\)")[0].split(" \\(")[0].trim());
        }

        for (int i = 1; i < titleSplit.length; ++i) {
            artistTag.append(";").append(titleSplit[i].split("\\)")[0].split(" \\(")[0].trim());
        }

        for (String andFilter : andFilters) {
            if (artistTag.toString().contains(andFilter)) {
                artistTag = new StringBuilder(artistTag.toString().replaceAll(andFilter, andFilter.replaceAll("&", "and")));
            }
        }

        artistTag = new StringBuilder(artistTag.toString().replaceAll(" & ", ";").replaceAll(", ", ";").trim());

        for (String andFilter : andFilters) {
            andFilter = andFilter.replaceAll("&", "and");
            if (artistTag.toString().contains(andFilter)) {
                artistTag = new StringBuilder(artistTag.toString().replaceAll(andFilter, andFilter.replaceAll("and", "&")));
            }
        }

        tag.setField(FieldKey.ARTIST, artistTag.toString());

        return artistTag.toString();
    }

    private static String[] splitTitleOrArtist(String str) {
        return str.split("\\(?[fF](ea)?t\\.? ");
    }

    private static boolean checkScriptError(Process process) throws IOException {
        String line;

        InputStream stdErr = process.getErrorStream();
        InputStreamReader isrErr = new InputStreamReader(stdErr);
        BufferedReader brErr = new BufferedReader(isrErr);

        while ((line = brErr.readLine()) != null) {
            System.err.println(line);
            return true;
        }

        return false;
    }

    private static Process startAndWaitForProcess(String... commands) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        Process process = processBuilder.start();
        process.waitFor();
        return process;
    }

    private static boolean updateXML(String xml, String musicPath, String title, String artist) {
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the
            // XML file
            dom = db.parse(xml);

            Element doc = dom.getDocumentElement();

            Node collection = doc.getElementsByTagName("COLLECTION").item(0);

            Element track = dom.createElement("TRACK");

            Attr artistAttr = dom.createAttribute("Artist");
            artistAttr.setNodeValue(artist);
            track.getAttributes().setNamedItem(artistAttr);


            Attr nameAttr = dom.createAttribute("Name");
            nameAttr.setNodeValue(title);
            track.getAttributes().setNamedItem(nameAttr);

            Attr locationAttr = dom.createAttribute("Location");
            musicPath = musicPath.replaceAll("\\\\", "/");
            locationAttr.setNodeValue("file://localhost/" + musicPath);
            track.getAttributes().setNamedItem(locationAttr);

            collection.appendChild(track);

            // Update entries amount
            Attr entriesAttr = dom.createAttribute("Entries");
            int entries = 0;
            for (int i = 0; i < collection.getChildNodes().getLength(); ++i) {
                if (collection.getChildNodes().item(i).getNodeName().equalsIgnoreCase("TRACK"))
                    ++entries;
            }
            entriesAttr.setNodeValue(String.valueOf(entries));
            collection.getAttributes().setNamedItem(entriesAttr);

            try {
                Transformer tr = TransformerFactory.newInstance().newTransformer();
                tr.setOutputProperty(OutputKeys.METHOD, "xml");
                tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

                // send DOM to file
                tr.transform(new DOMSource(dom),
                        new StreamResult(new FileOutputStream(xml)));

            } catch (TransformerException te) {
                System.out.println(te.getMessage());
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }

            return true;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return false;
    }
}
