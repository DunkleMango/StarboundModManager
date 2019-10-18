package data.mod;

import data.file.storage.ModDataFileManager;
import data.mod.exception.ModDateException;
import data.mod.exception.ModLoadingException;
import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import settings.AppSettingsCoordinator;
import web.HTTPModRequestException;
import web.SteamCommunicator;

import java.io.File;

public class ModData {
    public static final String KEY_ID = "publishedfileid";
    public static final String KEY_PREVIEW_IMAGE_URL = "preview_url";
    public static final String KEY_TITLE = "title";
    public static final String KEY_TAGS = "tags";
    private static final Logger logger = LogManager.getLogger("ModData");
    private final JSONObject data;

    /**
     * Creates {@link ModData} by calling the SteamAPI.
     * @param id The mod-id to request the data from.
     * @throws ModLoadingException When there was a problem requesting or processing the data.
     */
    public ModData(long id) throws ModLoadingException {
        SteamCommunicator steamCommunicator = new SteamCommunicator();
        try {
            this.data = steamCommunicator.postRequestMod(id);
            logger.debug("Loaded mod: {} from the SteamAPI.", id);
        } catch (HTTPModRequestException e) {
            logger.error("Failed to create mod.", e);
            throw new ModLoadingException("Failed to load mod from SteamAPI.");
        }
    }

    @Override
    public String toString() {
        return getTitle();
    }

    /**
     * Creates {@link ModData} by accessing the storage on this computer.
     * @param data The data to load from this computer.
     * @throws ModLoadingException When there was a problem loading the stored data.
     */
    public ModData(JSONObject data) throws ModLoadingException {
        if (ModDataValidator.isModDataInvalid(data))
            throw new ModLoadingException("Failed to load mod from storage.");
        this.data = data;
    }

    public void addToFileManager(ModDataFileManager modDataFileManager) {
        modDataFileManager.addToData(this.data);
    }

    /**
     * Returns the id of this mod.
     * If data is corrupted, returns -1.
     * @return id The id of this mod.
     */
    public long getId() {
        return this.data.optLong(KEY_ID, -1);
    }

    /**
     * Returns the title of this mod.
     * If data is corrupted, returns a {@link String} representing this problem.
     * @return title The title of this mod.
     */
    public String getTitle() {
        return this.data.optString(KEY_TITLE, "[ corrupted title ]");
    }

    public Image getPreviewImage() {
        Image resultImage = new Image(this.data.optString(KEY_PREVIEW_IMAGE_URL));
        if (resultImage.isError()) {
            resultImage = new Image(getClass().getResourceAsStream("/resources/application/MissingPreviewImage.svg"));
        }
        return resultImage;
    }

    public boolean isUpdateAvailable() {
        try {
            return (getLastChangedWorkshop() - getLastChangedServerMod()) > 0;
        } catch (ModDateException e) {
            logger.error("Failed to gather information about modification date of mod-file. Not advising update.");
            return false;
        }
    }

    public long getLastChangedWorkshop() throws ModDateException {
        final File workshopDirectory = AppSettingsCoordinator.getInstance().getWorkshopDirectory();
        return getLastChanged(workshopDirectory);
    }

    public long getLastChangedServerMod() throws ModDateException {
        final File serverModDirectory = AppSettingsCoordinator.getInstance().getServerModDirectory();
        return getLastChanged(serverModDirectory);
    }

    private long getLastChanged(File parentDir) throws ModDateException {
        final File[] files = parentDir.listFiles((dir, name) -> dir.isDirectory()
                && name.contentEquals(String.valueOf(getId())));
        if (files == null || files.length < 1)
            throw new ModDateException("Unable to retrieve date of \"last changed\" property of file");
        final File targetDir = files[0];
        return targetDir.lastModified();
    }
}
