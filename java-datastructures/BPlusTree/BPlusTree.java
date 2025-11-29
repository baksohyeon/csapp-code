package BPlusTree;

public class BPlusTree<K extends Comparable<K>, V> {
    private BPlusTreeNode<K, V> root;
    private final int order;

    public BPlusTree(int order) {
        if (order < 3) {
            throw new IllegalArgumentException("Order must be at least 3.");
        }
        this.order = order;
        this.root = new BPlusTreeNode<>(order, true);
    }

    // Search for a value associated with a key
    @SuppressWarnings("unchecked")
    public V search(K key) {
        BPlusTreeNode<K, V> currentNode = root;
        // Traverse down to the leaf node
        while (!currentNode.isLeaf) {
            int index = currentNode.findIndex(key);
            if (index < currentNode.keyCount && key.compareTo(currentNode.keys[index]) == 0) {
                 // In B+ Tree, internal nodes might have the key, but we need to go right
                currentNode = (BPlusTreeNode<K, V>) currentNode.pointers[index + 1];
            } else {
                currentNode = (BPlusTreeNode<K, V>) currentNode.pointers[index];
            }
        }
        
        // Search for the key in the leaf node
        int index = currentNode.findIndex(key);
        if (index < currentNode.keyCount && currentNode.keys[index].compareTo(key) == 0) {
            return (V) currentNode.pointers[index + 1];
        }
        
        return null; // Key not found
    }

    // Insert method placeholder - implementation is complex
    public void insert(K key, V value) {
        // To be implemented
        System.out.println("Insert functionality is not yet implemented.");
    }

    // Print the tree structure (for debugging)
    public void printTree() {
        // To be implemented
        System.out.println("Print functionality is not yet implemented.");
    }
}
