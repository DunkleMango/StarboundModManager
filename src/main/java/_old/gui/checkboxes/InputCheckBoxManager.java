package _old.gui.checkboxes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InputCheckBoxManager extends CheckBoxManager {
    private static final Logger logger = LogManager.getLogger("InputCheckBoxManager");
    private static InputCheckBoxManager instance;

    private InputCheckBoxManager() {

    }

    public static InputCheckBoxManager getInstance() {
        if (instance == null) {
            instance = new InputCheckBoxManager();
        }
        return instance;
    }

    @Override
    public void debugLogCheckBoxes(String prefix) {
        logger.debug("{}: {}", prefix, this.filesChecked);
    }
}
