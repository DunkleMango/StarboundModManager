package managers.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class SettingsManager {

    public static final String INPUT_PATH = "input.path";
    public static final String OUTPUT_PATH = "output.path";
    private static final String SEPARATOR = System.getProperty("file.separator");
    private static final String DIR_PATH = new File(System.getProperty("user.home")
            + SEPARATOR + "StarboundModManager" + SEPARATOR).getAbsolutePath();
    private static final String FILE_PATH = DIR_PATH + "\\settings.properties";
    private static final String DESCRIPTION = "This file stores the settings of the StarboundModManager.";
    private static SettingsManager instance;
    private Properties settings = new Properties();
    private boolean directoriesCreated = false;

    private SettingsManager() {
        createDirectories();
        createFileIfNotExisting();
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    private void createDirectories() {
        File file = new File(DIR_PATH);
        if (!file.exists()) {
            directoriesCreated = file.mkdirs();
        } else {
            directoriesCreated = true;
        }
        if (!directoriesCreated) {
            System.out.println("[ERROR][INIT] Directory " + DIR_PATH + " could not be created.");
        }
    }

    private void createFileIfNotExisting() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("[ERROR][INIT] File " + FILE_PATH + " could not be created.");
            }
        }
    }

    public void loadSettings() {
        if (directoriesCreated) {
            try (FileInputStream in = new FileInputStream(FILE_PATH)) {
                settings.load(in);
            } catch (IOException e) {
                System.out.println("[ERROR][LOADING] " + FILE_PATH + " could not be located.");
            }
        }
    }

    public void saveSettings() {
        if (directoriesCreated) {
            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                settings.store(out, DESCRIPTION);
            } catch (IOException e) {
                System.out.println("[ERROR][SAVING] " + FILE_PATH + " could not be located.");
            }
        }
    }

    public String getSetting(String key) {
        return settings.getProperty(key);
    }

    public void setSetting(String key, String value) {
        settings.setProperty(key, value);
    }
}
