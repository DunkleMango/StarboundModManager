package com.github.dunklemango.starboundmodmanager.transfer;

import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTransferTask extends Task<TransferTaskInformation> {
    private static final Logger logger = LogManager.getLogger("FileTransferTask");
    private final List<File> inputFiles;
    private final List<File> outputFiles;
    private final double progressPerStep;

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
        progressBar.progressProperty().bind(this.progressProperty());

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected TransferTaskInformation call() throws Exception {
        TransferTaskInformation info = new TransferTaskInformation();
        double progress = 0.0;
        for (int i = 0; i < inputFiles.size(); i++) {
            File inputFile = inputFiles.get(i);
            File outputFile = outputFiles.get(i);
            if (inputFile.exists() && inputFile.isFile() && outputFile.getName().endsWith(".pak")) {
                try {
                    logger.debug("outputFile {}.", (outputFile.exists()) ? "exists" : "does not exist");
                    Files.deleteIfExists(outputFile.toPath());
                    logger.debug("copying file from \"{}\" to \"{}\".", inputFile, outputFile);
                    Files.copy(inputFile.toPath(), outputFile.toPath());
                } catch (IOException e) {
                    info.put(inputFile, e);
                }
            }
            progress += progressPerStep;
            updateProgress(progress, 1.0);
        }
        progress = 1.0;
        updateProgress(progress, 1.0);
        return info;
    }
}
