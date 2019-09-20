package com.github.dunklemango.starboundmodmanager.transfer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TransferTaskInformation {
    private static final Logger logger = LogManager.getLogger("TransferTaskInformation");
    private Map<File, Exception> faultingFiles;

    public TransferTaskInformation() {
        faultingFiles = new HashMap<>();
    }

    public void put(File key, Exception value) {
        if (value == null) return;
        this.faultingFiles.put(key, value);
    }

    public void print() {
        if (faultingFiles.isEmpty()) {
            logger.debug("All files were transferred successfully.");
            return;
        }
        logger.error("Printing files that could not be copied to the desired location. Labeled FTTI.");
        faultingFiles.forEach((file, exception) -> {
            logger.error("[FTTI] File \"{}\" caused an error:", file, exception);
        });
    }
}
