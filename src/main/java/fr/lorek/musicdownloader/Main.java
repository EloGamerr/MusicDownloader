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

import java.io.*;

public class Main {
    private final static String[] andFilters = {"Lucas & Steve"};

    public static void main(String[] args) throws IOException, InterruptedException, CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, CannotWriteException {
        String musicsPath = "C:\\Users\\elgam\\Desktop\\musics";

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("pip", "install", "moviepy", "youtube_title_parse");
        Process process = processBuilder.start();
        process.waitFor();

        String home = System.getProperty("user.home");
        processBuilder = new ProcessBuilder();
        processBuilder.command("python3", "pytube\\script.py", "https://www.youtube.com/watch?v=-CVn3-3g_BI", home + "\\Downloads");

        process = processBuilder.start();

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

            String artistPath = tag.getFirst(FieldKey.ARTIST).split(";")[0].trim().replaceAll(" ", "_");
            File copied = new File(musicsPath + "\\" + artistPath + "\\" + file.getName());
            FileUtils.copyFile(file, copied);
        }
    }

    private static void updateTitleTag(Tag tag, String title) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        title = titleSplit[0].trim();
        tag.setField(FieldKey.TITLE, title);
    }

    private static void updateArtistTag(Tag tag, String title, String artist) throws FieldDataInvalidException {
        String[] titleSplit = splitTitleOrArtist(title);
        String[] artistSplit = splitTitleOrArtist(artist);
        String artistTag = artistSplit[0].trim();

        for (int i = 1; i < artistSplit.length; ++i) {
            artistTag += ";" + artistSplit[i].split("\\)")[0].split(" \\(")[0].trim();
        }

        for (int i = 1; i < titleSplit.length; ++i) {
            artistTag += ";" + titleSplit[i].split("\\)")[0].split(" \\(")[0].trim();
        }

        for (String andFilter : andFilters) {
            if (artistTag.contains(andFilter)) {
                artistTag = artistTag.replaceAll(andFilter, andFilter.replaceAll("&", "and"));
            }
        }

        artistTag = artistTag.replaceAll(" & ", ";").replaceAll(", ", ";").trim();

        for (String andFilter : andFilters) {
            andFilter = andFilter.replaceAll("&", "and");
            if (artistTag.contains(andFilter)) {
                artistTag = artistTag.replaceAll(andFilter, andFilter.replaceAll("and", "&"));
            }
        }

        tag.setField(FieldKey.ARTIST, artistTag);
    }

    private static String[] splitTitleOrArtist(String str) {
        return str.split("\\(?[fF]eat\\. ");
    }
}
