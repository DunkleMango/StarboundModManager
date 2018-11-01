package gui;

import gui.cells.CheckBoxCell;
import gui.cells.RepresentingType;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import managers.checkboxes.InputCheckBoxManager;
import managers.checkboxes.OutputCheckBoxManager;
import managers.settings.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Optional;

public class MainFrame extends Application {

    private ObservableList<String> namedInputDirList = FXCollections.observableArrayList();
    private ObservableList<String> namedOutputFileList = FXCollections.observableArrayList();
    private ObservableMap<String, File> inputDirMap = FXCollections.observableHashMap();
    private ObservableMap<String, File> outputFileMap = FXCollections.observableHashMap();
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
        grid.setPadding(new Insets(25, 25, 25, 25));

        loadSettings();
        inputField = new TextField(inputPath.getAbsolutePath());
        outputField = new TextField(outputPath.getAbsolutePath());
        if (inputPath.exists()) addDirectoriesOfPath(inputPath, namedInputDirList, inputDirMap);
        if (outputPath.exists()) addFilesOfPath(outputPath, namedOutputFileList, outputFileMap);

        setupInputPanels(grid, primaryStage);
        setupOutputPanels(grid, primaryStage);

        setupTransferButton(grid);
        setupClearButton(grid);

        Scene scene = new Scene(grid, 500, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Starbound Mod Manager");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(event -> saveSettings());
        primaryStage.show();
    }

    private void setupTransferButton(GridPane grid) {
        Alert transferAlert = new Alert(Alert.AlertType.CONFIRMATION);
        transferAlert.setTitle("Confirmation of transfer");
        transferAlert.setHeaderText("Do you wish to transfer the following files?");

        Button transferButton = new Button("Transfer Selected");
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
                    transferFiles(dirsOfFilesToTransfer);
                    namedOutputFileList.clear();
                    addFilesOfPath(outputPath, namedOutputFileList, outputFileMap);
                }
            }
        });
        grid.add(transferButton, 0, 4);
    }

    private void transferFiles(ArrayList<String> dirsOfFilesToTransfer) {
        dirsOfFilesToTransfer.forEach(name -> {
            File inputDir = inputDirMap.get(name);
            if (inputDir.exists()) {
                File[] subFiles = inputDir.listFiles((subDir, subName) -> subName.toLowerCase().endsWith(".pak"));
                if (subFiles != null && subFiles.length == 1) {
                    File inputFile = subFiles[0];
                    File outputFile = new File(outputPath.getAbsolutePath() + "\\" +  name + ".pak");
                    if (inputFile.exists() && outputPath.exists()) {
                        try {
                            if (outputFile.exists()) outputFile.delete();
                            Files.copy(inputFile.toPath(), outputFile.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void setupClearButton(GridPane grid) {
        Alert clearAlert = new Alert(Alert.AlertType.CONFIRMATION);
        clearAlert.setTitle("Confirmation of file-deletion");
        clearAlert.setHeaderText("Do you wish to delete the following files?");

        Button clearButton = new Button("Delete selected");
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
                    filesToDelete.forEach(name -> deletePakFile(outputFileMap.get(name)));
                    namedOutputFileList.clear();
                    addFilesOfPath(outputPath, namedOutputFileList, outputFileMap);
                }
            }
        });
        grid.add(clearButton, 1,4);
    }

    private String getFilesAsText(ArrayList<String> files, RepresentingType type) {
        StringBuilder builder = new StringBuilder();
        files.forEach(name -> {
            File file;
            switch (type) {
                case INPUT:
                    file = inputDirMap.get(name);
                    break;
                case OUTPUT:
                    file = outputFileMap.get(name);
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
        if (outputPathString != null)  outputPath = new File(outputPathString);
    }

    private void setupOutputPanels(GridPane grid, Stage primaryStage) {
        Text rightTitle = new Text("Mods directory");
        rightTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(rightTitle, 1,0);

        outputListView.setCellFactory((view) -> new CheckBoxCell(RepresentingType.OUTPUT));
        grid.add(outputListView, 1, 1);

        grid.add(outputField, 1,2);

        DirectoryChooser outputDirectoryChooser = new DirectoryChooser();
        if (outputPath.exists() && outputPath.isDirectory()) outputDirectoryChooser.setInitialDirectory(outputPath);

        Button chooseOutput = new Button("Choose output directory");
        chooseOutput.setOnAction(event -> {
            File path = outputDirectoryChooser.showDialog(primaryStage);
            if (path != null && path.isDirectory()) {
                namedOutputFileList.clear();
                addFilesOfPath(path, namedOutputFileList, outputFileMap);
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

        grid.add(inputField, 0,2);

        DirectoryChooser inputDirectoryChooser = new DirectoryChooser();
        if (inputPath.exists() && inputPath.isDirectory()) inputDirectoryChooser.setInitialDirectory(inputPath);

        Button chooseInput = new Button("Choose input directory");
        chooseInput.setOnAction(event -> {
            File path = inputDirectoryChooser.showDialog(primaryStage);
            if (path != null && path.isDirectory()) {
                namedInputDirList.clear();
                addDirectoriesOfPath(path, namedInputDirList, inputDirMap);
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

    private void addDirectoriesOfPath(File path, ObservableList<String> namedDirList, ObservableMap<String, File> dirMap) {
        if (path == null) throw new IllegalArgumentException("[ERROR][UPDATING] Path was null.");
        if (!path.isDirectory()) throw new IllegalArgumentException("[ERROR][UPDATING] Path is not a directory.");
        File[] files = path.listFiles();
        for (int i = 0; i < files.length; i++) {
            File dir = files[i];
            if (dir.isDirectory()) {
                File[] subFiles = dir.listFiles((subDir, name) -> name.toLowerCase().endsWith(".pak"));
                if (subFiles != null && subFiles.length == 1) {
                    String dirName = dir.getName();
                    namedDirList.add(dirName);
                    dirMap.put(dirName, dir);
                }
            }
        }
    }

    private void addFilesOfPath(File path, ObservableList<String> namedFileList, ObservableMap<String, File> fileMap) {
        if (path == null) throw new IllegalArgumentException("[ERROR][UPDATING] Path was null.");
        if (!path.isDirectory()) throw new IllegalArgumentException("[ERROR][UPDATING] Path is not a directory.");
        File[] subFiles = path.listFiles((subFile, name) -> name.toLowerCase().endsWith(".pak"));
        if (subFiles != null) {
            for (int i = 0; i < subFiles.length; i++) {
                File file = subFiles[i];
                String fileName = file.getName();
                namedFileList.add(fileName);
                fileMap.put(fileName, file);
            }
        }
    }

}
