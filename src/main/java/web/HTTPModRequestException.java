package web;

import org.jetbrains.annotations.Contract;

/**
 * This {@link Exception} has to be thrown when a call to the SteamAPI, to request information about a mod, has failed.
 */
public class HTTPModRequestException extends Exception {
    public static enum ERROR_TYPE {
        BAD_REQUEST,
        UNKNOWN_MOD_ID
    }
    private final ERROR_TYPE type;

    public HTTPModRequestException(ERROR_TYPE type, String message) {
        super(message);
        this.type = type;
    }

    @Contract(pure = true)
    public ERROR_TYPE getType() {
        return type;
    }
}
