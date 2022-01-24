package fr.lorek.musicdownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private final Properties properties;

    public PropertiesManager() {
        this.properties = new Properties();
        File file = new File("data.properties");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            this.properties.load(new FileInputStream("data.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getXMLPath() {
        return this.properties.getProperty("xmlPath", "rekordbox.xml");
    }

    public String getMusicsFolder() {
        return this.properties.getProperty("musicsFolder", "musics");
    }

    public void setXMLPath(String xmlPath) {
        this.properties.setProperty("xmlPath", xmlPath);
    }

    public void setMusicsFolder(String musicsFolder) {
        this.properties.getProperty("musicsFolder", musicsFolder);
    }
}
