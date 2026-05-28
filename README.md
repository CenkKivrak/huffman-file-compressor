Adaptive File Compressor Using Huffman Coding

A lossless data compression application built entirely in Java. This project implements the classic Huffman Coding greedy algorithm to reduce file sizes by assigning variable-length binary prefix codes to characters based on their frequency.

Performance Metrics
During testing on standard English text, this compressor achieved:
* **Original Size:** 1277 bytes
* **Compressed Size:** 767 bytes
* **Space Saved:** ~39.94%
* **Lossless Verification:** 100% data integrity upon decompression.

Technical Concepts Applied
* **Trees & Nodes:** Custom-built Binary Tree structure for prefix code generation.
* **Greedy Strategy:** Utilizing Java's `PriorityQueue` (Min-Heap) to continuously merge the lowest-frequency characters, ensuring an optimal mathematical prefix map.
* **Pre-order Traversal:** Serializing the tree structure directly into the compressed `.bin` file header.
* **Bit-Level I/O:** Bypassing standard byte-limitations using bitwise operators (`<<`, `|`, `>>`, `&`) to pack custom bits into standard bytes for file writing, and unpacking them bit-by-bit for reading.

How It Works
1. **Frequency Analysis:** Reads the input file and tallies character occurrences.
2. **Tree Building:** Constructs a Huffman Tree where the most frequent characters sit closest to the root.
3. **Encoding:** Traverses the tree to generate unique binary paths for each character.
4. **Bit-Packing:** Writes the tree header and the heavily compressed binary sequence to an output file.
5. **Decompression:** Reconstructs the tree from the binary header and traverses it bit-by-bit to seamlessly reconstruct the original plain text.
