import java.io.OutputStream;
import java.io.IOException;

public class BitWriter {
    private OutputStream out;
    private int currentByte; // Our bucket
    private int numBits;     // How full the bucket is (0 to 8)

    public BitWriter(OutputStream out) {
        this.out = out;
        this.currentByte = 0;
        this.numBits = 0;
    }

    // Drops a single bit (0 or 1) into the bucket
    public void writeBit(int bit) throws IOException {
        // Shift left by 1 to make room, then insert the new bit using OR
        currentByte = (currentByte << 1) | bit;
        numBits++;

        // If the bucket is full (8 bits), write it to the file and empty it!
        if (numBits == 8) {
            out.write(currentByte);
            currentByte = 0;
            numBits = 0;
        }
    }

    // Helper method to write standard 8-bit ASCII characters (used for Leaf Nodes in the header)
    // We must write it bit-by-bit to ensure it aligns perfectly with the current bucket state.
    public void writeByte(int b) throws IOException {
        // Loop through the 8 bits of the character from left to right
        for (int i = 7; i >= 0; i--) {
            int bit = (b >> i) & 1; // Extract the specific bit
            writeBit(bit);
        }
    }

    // If we finish compressing but the final bucket is only half-full, 
    // we pad it with 0s and force it to write.
    public void flush() throws IOException {
        if (numBits > 0) {
            // Shift the remaining bits to the far left so they align correctly
            currentByte = currentByte << (8 - numBits);
            out.write(currentByte);
        }
        out.close();
    }
}