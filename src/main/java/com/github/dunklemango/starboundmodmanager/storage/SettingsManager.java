package com.github.dunklemango.starboundmodmanager.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public final class SettingsManager {
    public static final String STEAM_PATH = "path.steam";
    private static final Logger logger = LogManager.getLogger("SettingsManager");
    private static final String FILE_PATH = FileManager.DIR_PATH + "\\settings.properties";
    private static final String DESCRIPTION = "This file stores the settings of the StarboundModManager.";
    private static SettingsManager instance;
    private Properties settings = new Properties();
    private boolean directoriesCreated;

    private SettingsManager() {
        directoriesCreated = FileManager.createDirectories(FileManager.DIR_PATH);
        FileManager.createFileIfNotExisting(FILE_PATH);
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public void loadSettings() {
        if (directoriesCreated) {
            try (FileInputStream in = new FileInputStream(FILE_PATH)) {
                settings.load(in);
            } catch (IOException e) {
                logger.error("{} could not be located.", FILE_PATH);
            }
        }
    }

    public void saveSettings() {
        if (directoriesCreated) {
            try (FileOutputStream out = new FileOutputStream(FILE_PATH)) {
                settings.store(out, DESCRIPTION);
            } catch (IOException e) {
                logger.error("{} could not be located.", FILE_PATH);
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
