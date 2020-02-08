package dialog.filesystemaction;

import data.mod.ModData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Observable;

public class ModAction extends Observable {
    private static Logger logger = LogManager.getLogger("ModAction");
    private final ModData mod;
    private final ActionType actionType;
    private volatile ActionResultType actionResultType;

    public ModAction(ModData mod, ActionType actionType) {
        this.mod = mod;
        this.actionType = actionType;
        this.actionResultType = ActionResultType.INDETERMINATE;
    }

    public void execute() {
        this.actionResultType = executeDirectly();
        logger.debug("Action executed - result: {}", this.actionResultType);
        setChanged();
        notifyObservers();
        logger.debug("Notified {} Observer(s)", countObservers());
    }

    private ActionResultType executeDirectly() {
        switch (this.actionType) {
            case COPY:
                try {
                    boolean successful = this.mod.copyToServer();
                    return successful ? ActionResultType.SUCCESSFUL : ActionResultType.ERROR ;
                } catch (IOException e) {
                    logger.error("Failed to copy mod to server", e);
                    return ActionResultType.ERROR;
                }
            case DELETE:
                try {
                    boolean successful = this.mod.deleteServerSide();
                    return successful ? ActionResultType.SUCCESSFUL : ActionResultType.ERROR ;
                } catch (IOException e) {
                    logger.error("Failed to delete mod from server", e);
                    return ActionResultType.ERROR;
                }
        }
        logger.error("ActionType has no handler!");
        return ActionResultType.ERROR;
    }

    public String getModTitle() {
        return this.mod.getTitle();
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public ActionResultType getActionResultType() {
        return this.actionResultType;
    }

    @Override
    public String toString() {
        return "ModAction{" +
                "mod=" + mod +
                ", actionType=" + actionType +
                ", actionResultType=" + actionResultType +
                '}';
    }
}
