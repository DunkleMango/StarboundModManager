package transfer;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTransferTask {

    private final List<File> inputFiles;
    private final List<File> outputFiles;
    private final double progressPerStep;
    private List <File> faultingFiles = new ArrayList<>();

    public FileTransferTask(List<File> inputFiles, List<File> outputFiles) {
        if (inputFiles.size() != outputFiles.size()) throw new IllegalArgumentException("Size of inputFiles must match that of outputFiles!");
        this.inputFiles = inputFiles;
        this.outputFiles = outputFiles;
        this.progressPerStep = 1.0 / inputFiles.size();
    }

    public FileTransferTask(File inputFile, File outputFile) {
        this.inputFiles = new ArrayList<>();
        this.outputFiles = new ArrayList<>();
        this.inputFiles.add(inputFile);
        this.outputFiles.add(outputFile);
        this.progressPerStep = 1.0;
    }

    public void transferFiles(ProgressBar progressBar) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                double progress = 0.0;
                for (int i = 0; i < inputFiles.size(); i++) {
                    File inputFile = inputFiles.get(i);
                    File outputFile = outputFiles.get(i);
                    if (inputFile.exists() && inputFile.isFile() && outputFile.getName().endsWith(".pak")) {
                        try {
                            Files.deleteIfExists(outputFile.toPath());
                            Files.copy(inputFile.toPath(), outputFile.toPath());
                        } catch (IOException e) {
                            faultingFiles.add(inputFile);
                        }
                    }
                    progress += progressPerStep;
                    updateProgress(progress, 1.0);
                }
                progress = 1.0;
                updateProgress(progress, 1.0);
                return null;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

}