package com.github.dunklemango.starboundmodmanager.transfer;

import com.github.dunklemango.starboundmodmanager.MainFrame;
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
    private volatile Task<Void> task;

    public FileTransferTask(List<File> inputFiles, List<File> outputFiles) {
        if (inputFiles.size() != outputFiles.size()) throw new IllegalArgumentException("Size of inputFiles must match that of outputFiles!");
        this.inputFiles = inputFiles;
        this.outputFiles = outputFiles;
        this.progressPerStep = 1.0 / inputFiles.size();
        init();
    }

    public FileTransferTask(File inputFile, File outputFile) {
        this.inputFiles = new ArrayList<>();
        this.outputFiles = new ArrayList<>();
        this.inputFiles.add(inputFile);
        this.outputFiles.add(outputFile);
        this.progressPerStep = 1.0;
        init();
    }

    private void init() {
        this.task = new Task<Void>() {
            @Override
            protected Void call() {
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
    }

    public void transferFiles(ProgressBar progressBar) {
        progressBar.progressProperty().bind(this.task.progressProperty());

        Thread thread = new Thread(this.task);
        thread.setDaemon(true);
        thread.start();
    }

    public Task<Void> getTask() {
        return this.task;
    }

}
