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

    // Get the order of the B+ Tree
    public int getOrder() {
        return order;
    }

    // Print the tree structure (for debugging)
    public void printTree() {
        if (root == null) {
            System.out.println("Tree is empty.");
            return;
        }
        
        System.out.println("B+ Tree Structure (Order: " + order + ")");
        System.out.println("================================");
        printTreeHelper(root, 0);
        System.out.println("================================");
    }
    
    // Helper method for recursive tree printing
    @SuppressWarnings("unchecked")
    private void printTreeHelper(BPlusTreeNode<K, V> node, int level) {
        if (node == null) return;
        
        // Print indentation for current level
        String indent = "  ".repeat(level);
        
        // Print node type and keys
        System.out.print(indent + (node.isLeaf ? "LEAF: " : "INTERNAL: "));
        System.out.print("[");
        for (int i = 0; i < node.keyCount; i++) {
            System.out.print(node.keys[i]);
            if (i < node.keyCount - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("]");
        
        // For leaf nodes, also print values
        if (node.isLeaf) {
            System.out.print(" -> Values: [");
            for (int i = 0; i < node.keyCount; i++) {
                if (node.pointers[i + 1] != null) {
                    System.out.print(node.pointers[i + 1]);
                    if (i < node.keyCount - 1) {
                        System.out.print(", ");
                    }
                }
            }
            System.out.print("]");
        }
        System.out.println();
        
        // Recursively print children for internal nodes
        if (!node.isLeaf) {
            for (int i = 0; i <= node.keyCount; i++) {
                if (node.pointers[i] != null) {
                    printTreeHelper((BPlusTreeNode<K, V>) node.pointers[i], level + 1);
                }
            }
        }
    }
}
