package com.github.dunklemango.starboundmodmanager.gui.checkboxes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class OutputCheckBoxManager extends CheckBoxManager {
    private static final Logger logger = LogManager.getLogger("OutputCheckBoxManager");
    private static OutputCheckBoxManager instance;

    private OutputCheckBoxManager() {

    }

    public static OutputCheckBoxManager getInstance() {
        if (instance == null) {
            instance = new OutputCheckBoxManager();
        }
        return instance;
    }

    @Override
    public void debugLogCheckBoxes(String prefix) {
        logger.debug("{}: {}", prefix, this.filesChecked);
    }
}
