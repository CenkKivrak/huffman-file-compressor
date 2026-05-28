import java.io.InputStream;
import java.io.IOException;

public class BitReader {
    private InputStream in;
    private int currentByte;
    private int numBitsRemaining; // How many bits are left in our current byte

    public BitReader(InputStream in) {
        this.in = in;
        this.currentByte = 0;
        this.numBitsRemaining = 0;
    }

    // Pours out a single bit (0 or 1)
    public int readBit() throws IOException {
        // If our current byte is empty, fetch the next one from the file
        if (numBitsRemaining == 0) {
            currentByte = in.read();
            if (currentByte == -1) {
                return -1; // -1 means we hit the true End Of File
            }
            numBitsRemaining = 8; // We have a fresh byte (8 bits)
        }

        // Extract the leftmost bit using a shift and a mask
        int bit = (currentByte >> (numBitsRemaining - 1)) & 1;
        numBitsRemaining--;
        
        return bit;
    }

    // Reads 8 bits in a row to reconstruct a standard ASCII character
    public int readByte() throws IOException {
        int value = 0;
        for (int i = 0; i < 8; i++) {
            int bit = readBit();
            if (bit == -1) break;
            // Shift left and insert the bit
            value = (value << 1) | bit;
        }
        return value;
    }
}