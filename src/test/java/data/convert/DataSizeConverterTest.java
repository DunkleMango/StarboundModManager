package data.convert;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataSizeConverterTest {

    @Test
    void testBytesToKiloBytes() {
        assertEquals(4, DataSizeConverter.bytesToKiloBytes(4096));
    }

    @Test
    void testMegaBytesToBytes() {
        assertEquals(4194304, DataSizeConverter.megaBytesToBytes(4));
    }
}