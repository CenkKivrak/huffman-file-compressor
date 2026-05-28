class HuffmanNode implements Comparable<HuffmanNode> {
    char character;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    // Constructor 1: For Leaf Nodes (actual characters from the file)
    public HuffmanNode(char character, int frequency) {
        this.character = character;
        this.frequency = frequency;
        this.left = null;
        this.right = null;
    }

    // Constructor 2: For Internal Nodes (merging two smaller nodes)
    public HuffmanNode(int frequency, HuffmanNode left, HuffmanNode right) {
        this.character = '\0'; // We use a null/dummy character for internal nodes
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    // Helper method: Useful later when generating codes and decompressing
    public boolean isLeaf() {
        return this.left == null && this.right == null;
    }

    // The core of the Greedy Algorithm: Sorting by lowest frequency first
    @Override
    public int compareTo(HuffmanNode other) {
        // This ensures the PriorityQueue acts as a Min-Heap based on frequency
        return this.frequency - other.frequency; 
    }
}