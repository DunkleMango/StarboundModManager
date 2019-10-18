package data.convert;

import org.jetbrains.annotations.Contract;

/**
 * Provides static methods to convert between different powers of 2 for Bytes.<br>
 * E.g. Bytes -> KiloBytes.
 */
public final class DataSizeConverter {
    private DataSizeConverter() {

    }

    /**
     * Converts the number of Bytes to number of KiloBytes.
     * @param bytes number of Bytes
     * @return kiloBytes number of KiloBytes
     */
    @Contract(pure = true)
    public static long bytesToKiloBytes(long bytes) {
        return bytes / 1024;
    }

    /**
     * Converts the number of MegaBytes to number of Bytes.
     * @param megaBytes number of MegaBytes
     * @return bytes number of Bytes
     */
    @Contract(pure = true)
    public static long megaBytesToBytes(long megaBytes) {
        return megaBytes * 1024 * 1024;
    }
}
