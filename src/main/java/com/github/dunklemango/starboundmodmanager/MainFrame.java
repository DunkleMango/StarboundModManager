package com.github.dunklemango.starboundmodmanager;

import com.github.dunklemango.starboundmodmanager.data.ModFile;
import com.github.dunklemango.starboundmodmanager.exceptions.ModFileGenerationException;
import com.github.dunklemango.starboundmodmanager.exceptions.ModFileNotFoundException;
import com.github.dunklemango.starboundmodmanager.gui.cells.CheckBoxCell;
import com.github.dunklemango.starboundmodmanager.gui.cells.RepresentingType;
import com.github.dunklemango.starboundmodmanager.gui.checkboxes.InputCheckBoxManager;
import com.github.dunklemango.starboundmodmanager.gui.checkboxes.OutputCheckBoxManager;
import com.github.dunklemango.starboundmodmanager.storage.SettingsManager;
import com.github.dunklemango.starboundmodmanager.storage.WorkshopCacheManager;
import com.github.dunklemango.starboundmodmanager.transfer.FileTransferTask;
import com.github.dunklemango.starboundmodmanager.workshop.WorkshopItem;
import com.github.dunklemango.starboundmodmanager.workshop.WorkshopItemManager;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.*;

public class MainFrame extends Application {
    private static final Logger logger = LogManager.getLogger("Application");
    private static final double FRAME_WIDTH = 500;
    private static final double FRAME_HEIGHT = 500;
    private static final double GRID_SIDE_PADDING = 25;
    private static VBox buttonVBox = new VBox();
    private static List<Integer> inputIds = new ArrayList<>();
    private static List<Integer> outputIds = new ArrayList<>();
    private ObservableList<String> inputTitles = FXCollections.observableArrayList();
    private ObservableList<String> outputTitles = FXCollections.observableArrayList();
    private ObservableSet<ModFile> inputDirList = FXCollections.observableSet();
    private ObservableSet<ModFile> outputFileList = FXCollections.observableSet();
    private ListView<String> outputListView = new ListView<>(outputTitles);
    private ListView<String> inputListView = new ListView<>(inputTitles);
    private File inputPath = new File("D:\\Programs\\Steam\\SteamApps\\workshop\\content\\211820");
    private File outputPath = new File("D:\\Programs\\Steam\\SteamApps\\common\\Starbound\\mods");
    private TextField inputField;
    private TextField outputField;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, GRID_SIDE_PADDING, 25, GRID_SIDE_PADDING));

        buttonVBox.setPrefWidth(FRAME_WIDTH / 2 - GRID_SIDE_PADDING);

        loadSettings();
        logger.debug("settings loaded");
        loadCachedWorkshopItems();
        logger.debug("workshop-item-cache loaded");

        inputField = new TextField(inputPath.getAbsolutePath());
        outputField = new TextField(outputPath.getAbsolutePath());
        if (inputPath.exists()) addDirectoriesOfPath(inputPath, inputIds, inputDirList);
        if (outputPath.exists()) addFilesOfPath(outputPath, inputIds, outputFileList);

        setupInputPanels(grid, primaryStage);
        setupOutputPanels(grid, primaryStage);

        setupTransferButton(grid, primaryStage);
        setupUpdateButton(grid, primaryStage);

        setupClearButton(grid);

        Scene scene = new Scene(grid, FRAME_WIDTH, FRAME_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Starbound Mod Manager");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> {
            saveSettings();
            saveCachedWorkshopItems();
        });
        updateAllTables();
        primaryStage.show();
    }

    private void setupUpdateButton(GridPane grid, Stage primaryStage) {
        Alert transferAlert = new Alert(Alert.AlertType.CONFIRMATION);
        transferAlert.setTitle("Confirmation of transfer");
        transferAlert.setHeaderText("Do you wish to transfer the following files?");

        Button updateButton = new Button("Update all");
        updateButton.setMinWidth(buttonVBox.getPrefWidth());
        updateButton.setOnAction(event -> {
            ArrayList<Integer> idsOfFilesToTransfer = new ArrayList<>();
            for (ModFile inputModFile : inputDirList) {
                ModFile outputModFile;
                try {
                    outputModFile = getModFile(outputFileList, inputModFile.getId());
                } catch (ModFileNotFoundException e) {
                    continue;
                }
                if (inputModFile != null && outputModFile != null && outputModFile.getId().equals(inputModFile.getId())
                        && inputModFile.isNewerThan(outputModFile)) {
                    idsOfFilesToTransfer.add(inputModFile.getId());
                }
            }
            if (!idsOfFilesToTransfer.isEmpty()) {
                transferAlert.setContentText(fuseTitlesForDisplay(idsOfFilesToTransfer, RepresentingType.INPUT));
                Optional<ButtonType> result = transferAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    transferFiles(idsOfFilesToTransfer, primaryStage);
                    updateOutputTable();
                }
            }
        });
        grid.add(updateButton, 1, 4);
    }

    private void updateAllTables() {
        logger.debug("updating tables..");
        logger.debug("updating input table..");
        updateInputTable();
        logger.debug("updating output table..");
        updateOutputTable();
        logger.debug("tables updated!");
    }

    private void updateInputTable() {
        inputIds.clear();
        addDirectoriesOfPath(inputPath, inputIds, inputDirList);
        inputTitles.clear();
        inputTitles.addAll(WorkshopCacheManager.getInstance().getTitlesFromIds(inputIds));
    }

    private void updateOutputTable() {
        outputIds.clear();
        addFilesOfPath(outputPath, outputIds, outputFileList);
        outputTitles.clear();
        outputTitles.addAll(WorkshopCacheManager.getInstance().getTitlesFromIds(outputIds));
    }

    private void setupTransferButton(GridPane grid, Stage primaryStage) {
        Alert transferAlert = new Alert(Alert.AlertType.CONFIRMATION);
        transferAlert.setTitle("Confirmation of transfer");
        transferAlert.setHeaderText("Do you wish to transfer the following files?");

        Button transferButton = new Button("Transfer Selected");
        transferButton.setMinWidth(buttonVBox.getPrefWidth());
        WorkshopCacheManager workshopCacheManager = WorkshopCacheManager.getInstance();
        transferButton.setOnAction(event -> {
            List<Integer> idsOfFilesToTransfer = new ArrayList<>();
            inputIds.forEach(id -> {
                InputCheckBoxManager manager = InputCheckBoxManager.getInstance();
                WorkshopCacheManager wcm = WorkshopCacheManager.getInstance();
                boolean isCheckboxSet = manager.get(wcm.get(id).getTitle());
                logger.debug("checkbox id: {}, value: {}", id, isCheckboxSet);
                if (isCheckboxSet) {
                    idsOfFilesToTransfer.add(id);
                }
            });
            logger.debug("dirs of files to transfer: {}", idsOfFilesToTransfer);
            if (!idsOfFilesToTransfer.isEmpty()) {
                transferAlert.setContentText(fuseTitlesForDisplay(idsOfFilesToTransfer ,RepresentingType.INPUT));
                Optional<ButtonType> result = transferAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    logger.debug("starting transfer..");
                    transferFiles(idsOfFilesToTransfer, primaryStage);
                    updateOutputTable();
                }
            }
        });
        grid.add(transferButton, 0, 4);
    }

    private void transferFiles(List<Integer> idsOfFilesToTransfer, Stage primaryStage) {
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("File-transfer");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, GRID_SIDE_PADDING, 25, GRID_SIDE_PADDING));
        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);

        grid.add(new Text("Progress of transfer:"), 0, 0);

        List<File> inputFiles = new ArrayList<>();
        List<File> outputFiles = new ArrayList<>();

        logger.debug("input dirs: {}", inputDirList);

        for (Integer id : idsOfFilesToTransfer) {
            File inputDir;
            try {
                inputDir = getModFile(inputDirList, id).getFile();

            } catch (ModFileNotFoundException e) {
                logger.error("modFile with title \"{}\" not found", id, e);
                continue;
            }
            if (inputDir != null && inputDir.exists()) {
                logger.debug("input dir exists!");
                File[] subFiles = inputDir.listFiles((subDir, subName) -> subName.toLowerCase().endsWith(".pak"));
                logger.debug("files in input dir: {}", Arrays.asList(subFiles));
                if (subFiles != null && subFiles.length == 1) {
                    File inputFile = subFiles[0];
                    File outputFile = new File(outputPath.getAbsolutePath() + "\\" + id + ".pak");
                    logger.debug("writing modFile: {}", outputFile);
                    if (inputFile.exists() && outputPath.exists()) {
                        logger.debug("input file \"{}\" and output path \"{}\" exists", inputFile, outputPath);
                        inputFiles.add(inputFile);
                        outputFiles.add(outputFile);
                    }
                }
            }
        }


        FileTransferTask fileTransferTask = new FileTransferTask(inputFiles, outputFiles);

        progressBar.progressProperty().addListener((obs, ov, nv) -> {
            if (nv.doubleValue() == 1.0) {
                dialog.hide();
            }
        });
        fileTransferTask.transferFiles(progressBar);
        grid.add(progressBar, 0, 1);
        fileTransferTask.getTask().setOnSucceeded(event -> updateOutputTable());

        Scene dialogScene = new Scene(grid, 300, 100);
        dialog.setScene(dialogScene);
        dialog.setResizable(false);
        dialog.show();
    }

    private void setupClearButton(GridPane grid) {
        Alert clearAlert = new Alert(Alert.AlertType.CONFIRMATION);
        clearAlert.setTitle("Confirmation of file-deletion");
        clearAlert.setHeaderText("Do you wish to delete the following files?");

        Button clearButton = new Button("Delete selected");
        clearButton.setMinWidth(buttonVBox.getPrefWidth());
        clearButton.setOnAction(event -> {
            ArrayList<Integer> idsOfFilesToDelete = new ArrayList<>();
            outputIds.forEach(id -> {
                OutputCheckBoxManager manager = OutputCheckBoxManager.getInstance();
                WorkshopCacheManager wcm = WorkshopCacheManager.getInstance();
                boolean isCheckboxSet = manager.get(wcm.get(id).getTitle());
                if (isCheckboxSet) {
                    idsOfFilesToDelete.add(id);
                }
            });
            if (!idsOfFilesToDelete.isEmpty()) {
                clearAlert.setContentText(fuseTitlesForDisplay(idsOfFilesToDelete, RepresentingType.OUTPUT));
                Optional<ButtonType> result = clearAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    idsOfFilesToDelete.forEach(name -> {
                        try {
                            deletePakFile(getModFile(outputFileList, name).getFile());
                        } catch (ModFileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    updateOutputTable();
                }
            }
        });
        grid.add(clearButton, 1, 5);
    }

    private String fuseTitlesForDisplay(List<Integer> idsOfFiles, RepresentingType type) {
        WorkshopCacheManager manager = WorkshopCacheManager.getInstance();
        StringBuilder builder = new StringBuilder();
        idsOfFiles.forEach(id -> {
                if (id != null) {
                    builder.append(manager.get(id).getTitle() + "\r\n");
                }
        });
        return builder.toString();
    }

    private ModFile getModFile(Collection<ModFile> collection, Integer id) throws ModFileNotFoundException {
        logger.debug("searching for modFileId: {}", id);
        for (ModFile modFile : collection) {
            Integer workshopItemId = modFile.getId();
            logger.debug("contained modFileId: {}", workshopItemId);
            if (workshopItemId.equals(id)) return modFile;
        }
        throw new ModFileNotFoundException(WorkshopCacheManager.getInstance().get(id).getTitle());
    }

    private void saveSettings() {
        SettingsManager settingsManager = SettingsManager.getInstance();
        settingsManager.setSetting(SettingsManager.INPUT_PATH, inputPath.getAbsolutePath());
        settingsManager.setSetting(SettingsManager.OUTPUT_PATH, outputPath.getAbsolutePath());
        settingsManager.saveSettings();
    }

    private void saveCachedWorkshopItems() {
        WorkshopCacheManager manager = WorkshopCacheManager.getInstance();
        manager.saveData();
    }

    private void loadSettings() {
        SettingsManager settingsManager = SettingsManager.getInstance();
        String inputPathString = settingsManager.getSetting(SettingsManager.INPUT_PATH);
        if (inputPathString != null) this.inputPath = new File(inputPathString);
        String outputPathString = settingsManager.getSetting(SettingsManager.OUTPUT_PATH);
        if (outputPathString != null) this.outputPath = new File(outputPathString);
    }

    private void loadCachedWorkshopItems() {
        WorkshopCacheManager manager = WorkshopCacheManager.getInstance();
        manager.loadData();
    }

    private void setupOutputPanels(GridPane grid, Stage primaryStage) {
        Text rightTitle = new Text("Mods directory");
        rightTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(rightTitle, 1, 0);

        outputListView.setCellFactory((view) -> new CheckBoxCell(RepresentingType.OUTPUT));
        grid.add(outputListView, 1, 1);

        grid.add(outputField, 1, 2);

        DirectoryChooser outputDirectoryChooser = new DirectoryChooser();
        if (outputPath.exists() && outputPath.isDirectory()) outputDirectoryChooser.setInitialDirectory(outputPath);

        Button chooseOutput = new Button("Choose output directory");
        chooseOutput.setMinWidth(buttonVBox.getPrefWidth());
        chooseOutput.setOnAction(event -> {
            File path = outputDirectoryChooser.showDialog(primaryStage);
            if (path != null && path.isDirectory()) {
                updateOutputTable();
                outputPath = path;
                updateOutputField();
            }
        });
        grid.add(chooseOutput, 1, 3);
    }

    private void setupInputPanels(GridPane grid, Stage primaryStage) {
        Text leftTitle = new Text("Workshop downloads");
        leftTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(leftTitle, 0, 0);

        inputListView.setCellFactory((view) -> new CheckBoxCell(RepresentingType.INPUT));
        grid.add(inputListView, 0, 1);

        grid.add(inputField, 0, 2);

        DirectoryChooser inputDirectoryChooser = new DirectoryChooser();
        if (inputPath.exists() && inputPath.isDirectory()) inputDirectoryChooser.setInitialDirectory(inputPath);

        Button chooseInput = new Button("Choose input directory");
        chooseInput.setMinWidth(buttonVBox.getPrefWidth());
        chooseInput.setOnAction(event -> {
            File path = inputDirectoryChooser.showDialog(primaryStage);
            if (path != null && path.isDirectory()) {
                updateInputTable();
                inputPath = path;
                updateInputField();
            }
        });
        grid.add(chooseInput, 0, 3);
    }

    private void deletePakFile(File file) {
        if (file != null && !file.isDirectory() && file.getAbsolutePath().endsWith(".pak")) {
            file.delete();
        }
    }

    private void updateOutputField() {
        outputField.clear();
        if (outputPath != null) {
            outputField.setText(outputPath.getAbsolutePath());
        }
    }

    private void updateInputField() {
        inputField.clear();
        if (inputPath != null) {
            inputField.setText(inputPath.getAbsolutePath());
        }
    }

    private void checkDirectoryCorrect(File path) {
        if (path == null) throw new IllegalArgumentException("Path was null.");
        if (!path.isDirectory()) throw new IllegalArgumentException("Path is not a directory.");
    }

    private Integer fileToModId(File file) throws NumberFormatException {
        String fileName = file.getName().replace(ModFile.MOD_FILE_EXTENSION, "");
        if (fileName.matches("[0-9]+")) {
            return Integer.valueOf(fileName);
        } else {
            logger.info("name of modFile contains illegal characters: \"{}\"", fileName);
            throw new NumberFormatException("name of modFile contains illegal characters");
        }
    }

    private void addDirectoriesOfPath(File path, List<Integer> idsOfDirs, ObservableSet<ModFile> dirSet) {
        logger.debug("adding directories of path {}", path);
        checkDirectoryCorrect(path);
        File[] files = path.listFiles((dir, name) -> dir.isDirectory());
        logger.debug("dirs of path {}", (files != null) ? Arrays.asList(files) : "null");
        List<Integer> workshopIds = new ArrayList<>();
        for (File dir : files) {
            ModFile modFile;
            try {
                Integer modId = fileToModId(dir);
                modFile = new ModFile(dir, modId);
                workshopIds.add(modId);
                dirSet.add(modFile);
            } catch (ModFileGenerationException | NumberFormatException e) {
                continue;
            }
        }

        WorkshopCacheManager manager = WorkshopCacheManager.getInstance();
        logger.debug("all workshopIds: {}", workshopIds);
        logger.debug("cached workshopIds: {}", manager.getCachedIds());
        List<Integer> workshopIdsToRetrieve = new ArrayList<>(workshopIds);
        workshopIdsToRetrieve.removeIf(id -> manager.containsKey(id));
        logger.debug("new workshopIds: {}", workshopIdsToRetrieve);

        List<WorkshopItem> workshopItemsToRetrieve = new ArrayList<>();
        workshopIdsToRetrieve.forEach(id -> workshopItemsToRetrieve.add(new WorkshopItem(id)));

        if (!workshopIdsToRetrieve.isEmpty()) {
            WorkshopItemManager.loadWorkshopDataFromSteam(workshopItemsToRetrieve);
            manager.putAll(workshopItemsToRetrieve);
        }

        idsOfDirs.addAll(workshopIds);
        logger.debug("names of workshopItems: {}", manager.getTitlesFromIds(workshopIds));
    }

    private void addFilesOfPath(File path, List<Integer> idsOfFiles, ObservableSet<ModFile> fileSet) {
        logger.debug("adding files of path {}", path);
        checkDirectoryCorrect(path);
        File[] files = path.listFiles((subFile, name) -> name.toLowerCase().endsWith(ModFile.MOD_FILE_EXTENSION));
        logger.debug("files on path: {}", (files != null) ? Arrays.asList(files) : "null");
        List<Integer> workshopIds = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                ModFile modFile;
                try {
                    Integer modId = fileToModId(file);
                    modFile = new ModFile(file, modId);
                    workshopIds.add(modId);
                    fileSet.add(modFile);
                } catch (ModFileGenerationException | NumberFormatException e) {
                    continue;
                }
            }
        }

        WorkshopCacheManager manager = WorkshopCacheManager.getInstance();
        logger.debug("all workshopIds: {}", workshopIds);
        logger.debug("cached workshopIds: {}", manager.getCachedIds());
        List<Integer> workshopIdsToRetrieve = new ArrayList<>(workshopIds);
        workshopIdsToRetrieve.removeIf(id -> manager.containsKey(id));
        logger.debug("new workshopIds: {}", workshopIdsToRetrieve);

        List<WorkshopItem> workshopItemsToRetrieve = new ArrayList<>();
        workshopIdsToRetrieve.forEach(id -> workshopItemsToRetrieve.add(new WorkshopItem(id)));

        if (!workshopIdsToRetrieve.isEmpty()) {
            WorkshopItemManager.loadWorkshopDataFromSteam(workshopItemsToRetrieve);
            manager.putAll(workshopItemsToRetrieve);
        }

        idsOfFiles.addAll(workshopIds);
        logger.debug("names of workshopItems: {}", manager.getTitlesFromIds(workshopIds));
    }

}
