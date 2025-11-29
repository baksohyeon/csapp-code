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
            int i;
            for (i = 0; i < currentNode.keyCount; i++) {
                if (key.compareTo(currentNode.keys[i]) < 0) {
                    break;
                }
            }
            int pointerIndexToFollow = i;
            if (pointerIndexToFollow >= currentNode.pointers.length || currentNode.pointers[pointerIndexToFollow] == null) {
                return null;
            }
            currentNode = (BPlusTreeNode<K, V>) currentNode.pointers[pointerIndexToFollow];
        }
        
        // Search for the key in the leaf node
        int index = currentNode.findIndex(key);
        if (index < currentNode.keyCount && currentNode.keys[index].compareTo(key) == 0) {
            return (V) currentNode.pointers[index + 1];
        }
        
        return null; // Key not found
    }

    // Insert method
    public void insert(K key, V value) {
        if (root.isLeaf) {
            insertIntoLeaf(root, key, value, null);
            return;
        }

        BPlusTreeNode<K, V> current = root;
        BPlusTreeNode<K, V> parent = null;

        // Traverse to find the leaf node
        while (!current.isLeaf) {
            parent = current;
            int index = current.findIndex(key);
            current = (BPlusTreeNode<K, V>) current.pointers[index];
            current.parent = parent; // Set parent for the child node
        }

        // Insert into the leaf node
        insertIntoLeaf(current, key, value, parent);
    }

    @SuppressWarnings("unchecked")
    private void insertIntoLeaf(BPlusTreeNode<K, V> leaf, K key, V value, BPlusTreeNode<K, V> parent) {
        if (leaf.keyCount < order - 1) { // Node has space
            int insertIndex = leaf.findIndex(key);
            // Shift keys and values to make space
            for (int i = leaf.keyCount; i > insertIndex; i--) {
                leaf.keys[i] = leaf.keys[i - 1];
                leaf.pointers[i + 1] = leaf.pointers[i]; // Shift values
            }
            leaf.keys[insertIndex] = key;
            leaf.pointers[insertIndex + 1] = value;
            leaf.keyCount++;
        } else { // Node is full, needs splitting
            splitLeafNode(leaf, key, value, parent);
        }
    }

    @SuppressWarnings("unchecked")
    private void splitLeafNode(BPlusTreeNode<K, V> leaf, K key, V value, BPlusTreeNode<K, V> parent) {
        // Create a temporary array to hold all keys and values, including the new one
        K[] tempKeys = (K[]) new Comparable[order];
        Object[] tempPointers = new Object[order + 1];

        int insertIndex = leaf.findIndex(key);
        int j = 0;
        for (int i = 0; i < leaf.keyCount; i++) {
            if (j == insertIndex) {
                tempKeys[j] = key;
                tempPointers[j + 1] = value;
                j++;
            }
            tempKeys[j] = leaf.keys[i];
            tempPointers[j + 1] = leaf.pointers[i + 1];
            j++;
        }
        if (j == insertIndex) { // If new key is largest
            tempKeys[j] = key;
            tempPointers[j + 1] = value;
        }

        // Clear the original leaf node
        leaf.keyCount = 0;
        for (int i = 0; i < order - 1; i++) {
            leaf.keys[i] = null;
            leaf.pointers[i + 1] = null;
        }

        // Distribute keys and values to original leaf and new leaf
        BPlusTreeNode<K, V> newLeaf = new BPlusTreeNode<>(order, true);
        int mid = (order - 1) / 2; // Midpoint for keys

        // Populate original leaf
        for (int i = 0; i <= mid; i++) { // Include mid for left node
            leaf.keys[i] = tempKeys[i];
            leaf.pointers[i + 1] = tempPointers[i + 1];
            leaf.keyCount++;
        }

        // Populate new leaf. The key to promote is the first key of the new leaf.
        K promotedKey = tempKeys[mid + 1];
        for (int i = mid + 1; i < order; i++) {
            newLeaf.keys[i - (mid + 1)] = tempKeys[i];
            newLeaf.pointers[i - (mid + 1) + 1] = tempPointers[i + 1];
            newLeaf.keyCount++;
        }

        // Link siblings (if this is a leaf node)
        newLeaf.pointers[0] = leaf.pointers[0]; // New leaf points to what old leaf pointed to
        leaf.pointers[0] = newLeaf; // Old leaf points to new leaf
        newLeaf.parent = parent;

        if (leaf == root) {
            BPlusTreeNode<K, V> newRoot = new BPlusTreeNode<>(order, false);
            newRoot.keys[0] = promotedKey;
            newRoot.pointers[0] = leaf;
            newRoot.pointers[1] = newLeaf;
            newRoot.keyCount = 1;
            root = newRoot;
            leaf.parent = newRoot;
            newLeaf.parent = newRoot;
        } else {
            insertIntoParent(parent, promotedKey, newLeaf);
        }
    }

    @SuppressWarnings("unchecked")
    private void insertIntoParent(BPlusTreeNode<K, V> parent, K key, BPlusTreeNode<K, V> rightChild) {
        if (parent.keyCount < order - 1) { // Parent has space
            int insertIndex = parent.findIndex(key);
            for (int i = parent.keyCount; i > insertIndex; i--) {
                parent.keys[i] = parent.keys[i - 1];
                parent.pointers[i + 1] = parent.pointers[i];
            }
            parent.keys[insertIndex] = key;
            parent.pointers[insertIndex + 1] = rightChild;
            parent.keyCount++;
            rightChild.parent = parent;
        } else { // Parent is full, needs splitting
            splitInternalNode(parent, key, rightChild);
        }
    }

    @SuppressWarnings("unchecked")
    private void splitInternalNode(BPlusTreeNode<K, V> internalNode, K key, BPlusTreeNode<K, V> rightChild) {
        // Create temporary arrays to hold all keys and pointers, including the new ones
        K[] tempKeys = (K[]) new Comparable[order];
        Object[] tempPointers = new Object[order + 1];

        // Find the index where the new key should be inserted in the keys array
        int insertKeyIndex = internalNode.findIndex(key);
        if (insertKeyIndex < 0) {
            insertKeyIndex = -insertKeyIndex - 1;
        }

        // Populate tempKeys and tempPointers by inserting the new key and rightChild
        // The new child is always associated with the key being inserted, it goes to the right of the key.
        // So, if key is inserted at tempKeys[i], rightChild is at tempPointers[i+1]
        
        int currentTempKeyIndex = 0;
        int currentTempPointerIndex = 0;

        // Copy pointers before the insertion point
        for (int i = 0; i <= insertKeyIndex; i++) { // For an insertion at index 'insertKeyIndex', pointers[0]...pointers[insertKeyIndex] are before it
            if (i < internalNode.keyCount + 1) { // Ensure not to go out of bounds for internalNode.pointers
                tempPointers[currentTempPointerIndex++] = internalNode.pointers[i];
            }
        }

        // Insert the new right child at the correct position (after the keys it separates)
        // The new right child is always associated with the new key, so it takes the place of internalNode.pointers[insertKeyIndex + 1]
        // All subsequent pointers shift right.
        tempPointers[insertKeyIndex + 1] = rightChild; 
        currentTempPointerIndex++;

        // Copy remaining pointers from internalNode.pointers, shifted right by one
        for (int i = insertKeyIndex + 1; i <= internalNode.keyCount; i++) {
            if (currentTempPointerIndex < order + 1) {
                 tempPointers[currentTempPointerIndex++] = internalNode.pointers[i];
            }
        }

        // Copy keys before the insertion point
        for (int i = 0; i < insertKeyIndex; i++) {
            tempKeys[currentTempKeyIndex++] = internalNode.keys[i];
        }

        // Insert the new key
        tempKeys[currentTempKeyIndex++] = key;

        // Copy remaining keys from internalNode.keys, shifted right by one
        for (int i = insertKeyIndex; i < internalNode.keyCount; i++) {
            tempKeys[currentTempKeyIndex++] = internalNode.keys[i];
        }

        // Clear the original internal node
        internalNode.keyCount = 0;
        for (int i = 0; i < order - 1; i++) { internalNode.keys[i] = null; }
        for (int i = 0; i < order; i++) { internalNode.pointers[i] = null; }

        // Distribute keys and pointers to original internal node and new internal node
        BPlusTreeNode<K, V> newInternalNode = new BPlusTreeNode<>(order, false);
        int midKeyIndex = (order - 1) / 2; // Midpoint for keys in allKeys

        // Populate original internal node (left child)
        internalNode.pointers[0] = tempPointers[0];
        if (internalNode.pointers[0] != null) {
            ((BPlusTreeNode<K, V>)internalNode.pointers[0]).parent = internalNode;
        }
        for (int l = 0; l < midKeyIndex; l++) {
            internalNode.keys[l] = tempKeys[l];
            internalNode.pointers[l + 1] = tempPointers[l + 1];
            if (internalNode.pointers[l + 1] != null) {
                ((BPlusTreeNode<K, V>)internalNode.pointers[l + 1]).parent = internalNode;
            }
            internalNode.keyCount++;
        }

        // The key to promote is at tempKeys[midKeyIndex]
        K promotedKey = tempKeys[midKeyIndex];

        // Populate new internal node (right child)
        newInternalNode.pointers[0] = tempPointers[midKeyIndex + 1];
        if (newInternalNode.pointers[0] != null) {
            ((BPlusTreeNode<K, V>)newInternalNode.pointers[0]).parent = newInternalNode;
        }
        for (int l = midKeyIndex + 1; l < order; l++) {
            newInternalNode.keys[l - (midKeyIndex + 1)] = tempKeys[l];
            newInternalNode.pointers[l - (midKeyIndex + 1) + 1] = tempPointers[l + 1];
            if (newInternalNode.pointers[l - (midKeyIndex + 1) + 1] != null) {
                ((BPlusTreeNode<K, V>)newInternalNode.pointers[l - (midKeyIndex + 1) + 1]).parent = newInternalNode;
            }
            newInternalNode.keyCount++;
        }

        // Handle parent or create new root
        if (internalNode == root) {
            BPlusTreeNode<K, V> newRoot = new BPlusTreeNode<>(order, false);
            newRoot.keys[0] = promotedKey;
            newRoot.pointers[0] = internalNode;
            newRoot.pointers[1] = newInternalNode;
            newRoot.keyCount = 1;
            root = newRoot;
            internalNode.parent = newRoot;
            newInternalNode.parent = newRoot;
        } else {
            insertIntoParent(internalNode.parent, promotedKey, newInternalNode);
        }
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
