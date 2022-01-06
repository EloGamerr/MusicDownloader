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

        Process process = startAndWaitForProcess("pip", "install", "moviepy", "youtube_title_parse");

        if (checkScriptError(process)) {
            return;
        }

        String home = System.getProperty("user.home");
        process = startAndWaitForProcess("python3", "pytube\\script.py", "https://www.youtube.com/watch?v=-CVn3-3g_BI", home + "\\Downloads");

        if (checkScriptError(process)) {
            return;
        }

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
        StringBuilder artistTag = new StringBuilder(artistSplit[0].trim());

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
    }

    private static String[] splitTitleOrArtist(String str) {
        return str.split("\\(?[fF]eat\\. ");
    }

    private static boolean checkScriptError(Process process) throws IOException {
        String line;

        InputStream stdErr = process.getErrorStream();
        InputStreamReader isrErr = new InputStreamReader(stdErr);
        BufferedReader brErr = new BufferedReader(isrErr);

        if ((line = brErr.readLine()) != null) {
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
}
