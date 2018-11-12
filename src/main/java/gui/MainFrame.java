package gui;

import data.ModFile;
import exceptions.ModFileGenerationException;
import exceptions.ModFileNotFoundException;
import gui.cells.CheckBoxCell;
import gui.cells.RepresentingType;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import managers.checkboxes.InputCheckBoxManager;
import managers.checkboxes.OutputCheckBoxManager;
import managers.settings.SettingsManager;
import transfer.FileTransferTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class MainFrame extends Application {

    private static final double FRAME_WIDTH = 500;
    private static final double FRAME_HEIGHT = 500;
    private static final double GRID_SIDE_PADDING = 25;
    private static VBox buttonVBox = new VBox();
    private ObservableList<String> namedInputDirList = FXCollections.observableArrayList();
    private ObservableList<String> namedOutputFileList = FXCollections.observableArrayList();
    private ObservableSet<ModFile> inputDirList = FXCollections.observableSet();
    private ObservableSet<ModFile> outputFileList = FXCollections.observableSet();
    private ListView<String> outputListView = new ListView<>(namedOutputFileList);
    private ListView<String> inputListView = new ListView<>(namedInputDirList);
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
        inputField = new TextField(inputPath.getAbsolutePath());
        outputField = new TextField(outputPath.getAbsolutePath());
        if (inputPath.exists()) addDirectoriesOfPath(inputPath, namedInputDirList, inputDirList);
        if (outputPath.exists()) addFilesOfPath(outputPath, namedOutputFileList, outputFileList);

        setupInputPanels(grid, primaryStage);
        setupOutputPanels(grid, primaryStage);

        setupTransferButton(grid, primaryStage);
        setupUpdateButton(grid, primaryStage);

        setupClearButton(grid);

        Scene scene = new Scene(grid, FRAME_WIDTH, FRAME_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Starbound Mod Manager");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> saveSettings());
        primaryStage.show();
    }

    private void setupUpdateButton(GridPane grid, Stage primaryStage) {
        Alert transferAlert = new Alert(Alert.AlertType.CONFIRMATION);
        transferAlert.setTitle("Confirmation of transfer");
        transferAlert.setHeaderText("Do you wish to transfer the following files?");

        Button updateButton = new Button("Update all");
        updateButton.setMinWidth(buttonVBox.getPrefWidth());
        updateButton.setOnAction(event -> {
            ArrayList<String> dirsOfFilesToTransfer = new ArrayList<>();


            for (ModFile inputModFile : inputDirList) {
                ModFile outputModFile = null;
                try {
                    outputModFile = getModFile(outputFileList, inputModFile.getName() + ".pak");
                } catch (ModFileNotFoundException e) {
                    continue;
                }
                if (inputModFile != null && outputModFile != null && outputModFile.getName().equals(inputModFile.getName() + ".pak")
                        && inputModFile.isNewerThan(outputModFile)) {
                    dirsOfFilesToTransfer.add(inputModFile.getName());

                }
            }
            if (!dirsOfFilesToTransfer.isEmpty()) {
                transferAlert.setContentText(getFilesAsText(dirsOfFilesToTransfer, RepresentingType.INPUT));
                Optional<ButtonType> result = transferAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    transferFiles(dirsOfFilesToTransfer, primaryStage);
                    namedOutputFileList.clear();
                    addFilesOfPath(outputPath, namedOutputFileList, outputFileList);
                }
            }
        });
        grid.add(updateButton, 1, 4);
    }

    private void setupTransferButton(GridPane grid, Stage primaryStage) {
        Alert transferAlert = new Alert(Alert.AlertType.CONFIRMATION);
        transferAlert.setTitle("Confirmation of transfer");
        transferAlert.setHeaderText("Do you wish to transfer the following files?");

        Button transferButton = new Button("Transfer Selected");
        transferButton.setMinWidth(buttonVBox.getPrefWidth());
        transferButton.setOnAction(event -> {
            ArrayList<String> dirsOfFilesToTransfer = new ArrayList<>();
            namedInputDirList.forEach(name -> {
                InputCheckBoxManager manager = InputCheckBoxManager.getInstance();
                if (manager.get(name) != null && manager.get(name)) {
                    dirsOfFilesToTransfer.add(name);
                }
            });
            if (!dirsOfFilesToTransfer.isEmpty()) {
                transferAlert.setContentText(getFilesAsText(dirsOfFilesToTransfer, RepresentingType.INPUT));
                Optional<ButtonType> result = transferAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    transferFiles(dirsOfFilesToTransfer, primaryStage);
                    namedOutputFileList.clear();
                    addFilesOfPath(outputPath, namedOutputFileList, outputFileList);
                }
            }
        });
        grid.add(transferButton, 0, 4);
    }

    private void transferFiles(ArrayList<String> dirsOfFilesToTransfer, Stage primaryStage) {
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

        for (String name : dirsOfFilesToTransfer) {
            File inputDir;
            try {
                inputDir = getModFile(inputDirList, name).getFile();
            } catch (ModFileNotFoundException e) {
                continue;
            }
            if (inputDir != null && inputDir.exists()) {
                File[] subFiles = inputDir.listFiles((subDir, subName) -> subName.toLowerCase().endsWith(".pak"));
                if (subFiles != null && subFiles.length == 1) {
                    File inputFile = subFiles[0];
                    File outputFile = new File(outputPath.getAbsolutePath() + "\\" + name + ".pak");
                    if (inputFile.exists() && outputPath.exists()) {
                        inputFiles.add(inputFile);
                        outputFiles.add(outputFile);
                    }
                }
            }
        }


        FileTransferTask fileTransferTask = new FileTransferTask(inputFiles, outputFiles);

        fileTransferTask.transferFiles(progressBar);

        grid.add(progressBar, 0, 1);

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
            ArrayList<String> filesToDelete = new ArrayList<>();
            namedOutputFileList.forEach(name -> {
                OutputCheckBoxManager manager = OutputCheckBoxManager.getInstance();
                if (manager.get(name) != null && manager.get(name)) {
                    filesToDelete.add(name);
                }
            });
            if (!filesToDelete.isEmpty()) {
                clearAlert.setContentText(getFilesAsText(filesToDelete, RepresentingType.OUTPUT));
                Optional<ButtonType> result = clearAlert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    filesToDelete.forEach(name -> {
                        try {
                            deletePakFile(getModFile(outputFileList, name).getFile());
                        } catch (ModFileNotFoundException e) {
                            e.printStackTrace();
                        }
                    });
                    namedOutputFileList.clear();
                    addFilesOfPath(outputPath, namedOutputFileList, outputFileList);
                }
            }
        });
        grid.add(clearButton, 1, 5);
    }

    private String getFilesAsText(ArrayList<String> files, RepresentingType type) {
        StringBuilder builder = new StringBuilder();
        files.forEach(name -> {
            File file = null;
            switch (type) {
                case INPUT:
                    try {
                        file = getModFile(inputDirList, name).getFile();
                    } catch (ModFileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                case OUTPUT:
                    try {
                        file = getModFile(outputFileList, name).getFile();
                    } catch (ModFileNotFoundException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    file = null;
                    break;
            }
            if (file != null) {
                builder.append(file.getAbsolutePath() + "\r\n");
            }
        });
        return builder.toString();
    }

    private ModFile getModFile(Collection<ModFile> collection, String name) throws ModFileNotFoundException {
        for (ModFile modFile : collection) {
            if (modFile.getName().equals(name)) return modFile;
        }
        throw new ModFileNotFoundException(name);
    }

    private void saveSettings() {
        SettingsManager settingsManager = SettingsManager.getInstance();
        String settingsInput = settingsManager.getSetting(SettingsManager.INPUT_PATH);
        String settingsOutput = settingsManager.getSetting(SettingsManager.OUTPUT_PATH);
        if (settingsInput == null || settingsOutput == null || !settingsInput.equals(inputPath.getAbsolutePath())
                || settingsOutput.equals(outputPath.getAbsolutePath())) {
            settingsManager.setSetting(SettingsManager.INPUT_PATH, inputPath.getAbsolutePath());
            settingsManager.setSetting(SettingsManager.OUTPUT_PATH, outputPath.getAbsolutePath());
            settingsManager.saveSettings();
        }
    }

    private void loadSettings() {
        SettingsManager settingsManager = SettingsManager.getInstance();
        String inputPathString = settingsManager.getSetting(SettingsManager.INPUT_PATH);
        if (inputPathString != null) inputPath = new File(inputPathString);
        String outputPathString = settingsManager.getSetting(SettingsManager.OUTPUT_PATH);
        if (outputPathString != null) outputPath = new File(outputPathString);
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
                namedOutputFileList.clear();
                addFilesOfPath(path, namedOutputFileList, outputFileList);
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
                namedInputDirList.clear();
                addDirectoriesOfPath(path, namedInputDirList, inputDirList);
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
        if (path == null) throw new IllegalArgumentException("[ERROR][UPDATING] Path was null.");
        if (!path.isDirectory()) throw new IllegalArgumentException("[ERROR][UPDATING] Path is not a directory.");
    }

    private void addDirectoriesOfPath(File path, ObservableList<String> namedDirList, ObservableSet<ModFile> dirList) {
        checkDirectoryCorrect(path);
        File[] files = path.listFiles();
        for (File dir : files) {
            if (dir.isDirectory()) {
                String dirName = dir.getName();
                ModFile modFile;
                try {
                    modFile = new ModFile(dir, dirName);
                } catch (ModFileGenerationException e) {
                    continue;
                }
                namedDirList.add(dirName);
                dirList.add(modFile);
            }
        }
    }

    private void addFilesOfPath(File path, ObservableList<String> namedFileList, ObservableSet<ModFile> fileMap) {
        checkDirectoryCorrect(path);
        File[] subFiles = path.listFiles((subFile, name) -> name.toLowerCase().endsWith(".pak"));
        if (subFiles != null) {
            for (int i = 0; i < subFiles.length; i++) {
                File file = subFiles[i];
                String fileName = file.getName();
                ModFile modFile;
                try {
                    modFile = new ModFile(file, fileName);
                } catch (ModFileGenerationException e) {
                    continue;
                }
                namedFileList.add(fileName);
                fileMap.add(modFile);
            }
        }
    }

}
