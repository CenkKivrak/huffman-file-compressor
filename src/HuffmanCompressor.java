import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanCompressor {

    public static Map<Character, Integer> buildFrequencyMap(String filePath) {
        Map<Character, Integer> frequencyMap = new HashMap<>();

        // Try-with-resources ensures the BufferedReader is closed automatically
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            int characterCode;
            
            // Loop until the end of the file (-1)
            while ((characterCode = reader.read()) != -1) {
                // Cast the integer back to an actual character
                char c = (char) characterCode;
                
                // If 'c' is in the map, get its current count. If not, default to 0. Then add 1.
                frequencyMap.put(c, frequencyMap.getOrDefault(c, 0) + 1);
            }
            
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }

        return frequencyMap;
    }
    
    // ==========================================
    // PHASE 2: The Greedy Builder	
    // ==========================================
    public static HuffmanNode buildTree(Map<Character, Integer> frequencyMap) {
        // 1. Initialize the Priority Queue
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();

        // 2. Iterate through the Map and create Leaf Nodes
        // Map.Entry is the standard Java way to loop through keys and values together
        for (Map.Entry<Character, Integer> entry : frequencyMap.entrySet()) {
            char character = entry.getKey();
            int frequency = entry.getValue();
            
            // Create a leaf node and add it to the queue
            queue.add(new HuffmanNode(character, frequency));
        }

        // Safety check: If the file was completely empty
        if (queue.isEmpty()) {
            return null;
        }

        // 3. The Greedy Loop
        while (queue.size() > 1) {
            // Remove the two nodes with the lowest frequencies
            HuffmanNode leftNode = queue.poll();
            HuffmanNode rightNode = queue.poll();

            // Calculate the combined frequency
            int combinedFreq = leftNode.frequency + rightNode.frequency;

            // Create a new internal parent node
            // (Using Constructor 2: combined frequency, left child, right child)
            HuffmanNode parentNode = new HuffmanNode(combinedFreq, leftNode, rightNode);

            // Add the new parent node back into the queue
            queue.add(parentNode);
        }

        // 4. Return the final remaining node (The Root)
        return queue.poll();
    }
    // ==========================================
    // PHASE 3: The Code Dictionary
    // ==========================================
    public static void generateCodes(HuffmanNode node, String currentPath, Map<Character, String> dictionary) {
        // 1. Base Case / Safety Check: If the tree is empty or we hit a null child
        if (node == null) {
            return;
        }

        // 2. The Leaf Node Check (Our "dead end")
        if (node.isLeaf()) {
            // Save the character and the path we took to get here
            dictionary.put(node.character, currentPath);
            return; // Go back up the tree
        }

        // 3. The Recursive Exploration for Internal Nodes
        // Go Left: Append "0" to our current path
        generateCodes(node.left, currentPath + "0", dictionary);
        
        // Go Right: Append "1" to our current path
        generateCodes(node.right, currentPath + "1", dictionary);
    }

    // ==========================================
    // PHASE 4: File Compression
    // ==========================================
    
    // 4A: Pre-Order Traversal to write the Tree Header
    private static void writeTreeHeader(HuffmanNode node, BitWriter writer) throws Exception {
        if (node.isLeaf()) {
            writer.writeBit(1); // '1' indicates a Leaf Node
            writer.writeByte(node.character); // Write the actual 8-bit character
        } else {
            writer.writeBit(0); // '0' indicates an Internal Node
            writeTreeHeader(node.left, writer);
            writeTreeHeader(node.right, writer);
        }
    }

    // 4B: The Main Compression Pipeline
    public static void compress(String inputPath, String outputPath, HuffmanNode root, Map<Character, String> dictionary) {
        // Use try-with-resources for standard streams
        try (java.io.FileInputStream fis = new java.io.FileInputStream(inputPath);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(outputPath)) {
            
            // Wrap our FileOutputStream in our custom BitWriter
            BitWriter writer = new BitWriter(fos);

            // 1. WRITE THE HEADER (The Tree)
            writeTreeHeader(root, writer);

            // 2. WRITE THE ENCODED TEXT
            int characterCode;
            while ((characterCode = fis.read()) != -1) {
                char c = (char) characterCode;
                String binaryCode = dictionary.get(c); // Look up the custom code
                
                // Write the string code as actual hardware bits
                for (char bitChar : binaryCode.toCharArray()) {
                    if (bitChar == '0') {
                        writer.writeBit(0);
                    } else {
                        writer.writeBit(1);
                    }
                }
            }

            // 3. FLUSH REMAINDER & CLOSE
            writer.flush();
            System.out.println("Compression Successful! Saved to " + outputPath);

        } catch (Exception e) {
            System.err.println("Compression Failed: " + e.getMessage());
        }
    }

 // ==========================================
    // PHASE 5: File Decompression
    // ==========================================
    
    // 5A: Rebuild the Tree from the Header
    private static HuffmanNode readTreeHeader(BitReader reader) throws Exception {
        int bit = reader.readBit();
        
        if (bit == 1) {
            // It's a Leaf Node! Read the next 8 bits to get the character.
            char character = (char) reader.readByte();
            return new HuffmanNode(character, -1); // Frequency doesn't matter anymore
        } else {
            // It's an Internal Node ('0'). Recursively build left, then right.
            HuffmanNode leftChild = readTreeHeader(reader);
            HuffmanNode rightChild = readTreeHeader(reader);
            return new HuffmanNode(-1, leftChild, rightChild);
        }
    }

    // 5B: The Main Decompression Pipeline
    public static void decompress(String compressedPath, String decodedPath, int totalCharsExpected) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(compressedPath);
             java.io.FileWriter writer = new java.io.FileWriter(decodedPath)) {
            
            BitReader reader = new BitReader(fis);

            // 1. REBUILD THE TREE
            HuffmanNode root = readTreeHeader(reader);

            // 2. DECODE THE TEXT
            int charsDecoded = 0;
            HuffmanNode currentNode = root;

            // Keep reading bits until we have found every character
            while (charsDecoded < totalCharsExpected) {
                int bit = reader.readBit();
                if (bit == -1) break; // Safety check for EOF

                // Traverse the tree: 0 goes left, 1 goes right
                if (bit == 0) {
                    currentNode = currentNode.left;
                } else {
                    currentNode = currentNode.right;
                }

                // If we hit a leaf, we found our character!
                if (currentNode.isLeaf()) {
                    writer.write(currentNode.character);
                    charsDecoded++;
                    
                    // Reset back to the root for the next character
                    currentNode = root;
                }
            }

            System.out.println("Decompression Successful! Saved to " + decodedPath);

        } catch (Exception e) {
            System.err.println("Decompression Failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        String plainTextFile = "test1.txt"; 
        String compressedBinFile = "encoded.bin"; 
        String decompressedFile = "decoded.txt";
        
        System.out.println("==================================");
        System.out.println("  HUFFMAN COMPRESSION PIPELINE    ");
        System.out.println("==================================");

        // 1. Count the characters
        Map<Character, Integer> freqMap = buildFrequencyMap(plainTextFile);
        
        // Calculate total characters for decompression safety
        int totalChars = 0;
        for (int count : freqMap.values()) {
            totalChars += count;
        }
        
        // 2 & 3. Build Tree & Dictionary
        HuffmanNode root = buildTree(freqMap);
        Map<Character, String> dictionary = new HashMap<>();
        generateCodes(root, "", dictionary);

        // 4. Compress
        compress(plainTextFile, compressedBinFile, root, dictionary);

        // 5. Decompress
        decompress(compressedBinFile, decompressedFile, totalChars);

        // ==========================================
        // Objective 2.1: Compression Ratio Calculator
        // ==========================================
        java.io.File original = new java.io.File(plainTextFile);
        java.io.File compressed = new java.io.File(compressedBinFile);
        
        long originalBytes = original.length();
        long compressedBytes = compressed.length();
        double spaceSaved = 100.0 * (1.0 - ((double) compressedBytes / originalBytes));

        System.out.println("\n--- COMPRESSION STATISTICS ---");
        System.out.println("Original Size: " + originalBytes + " bytes");
        System.out.println("Compressed Size: " + compressedBytes + " bytes");
        System.out.printf("Space Saved: %.2f%%\n", spaceSaved);
        System.out.println("==================================");
    }
}