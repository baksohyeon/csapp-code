package BPlusTree;

import java.util.Arrays;

class BPlusTreeNode<K extends Comparable<K>, V> {
    boolean isLeaf;
    int keyCount;
    K[] keys;
    Object[] pointers; // Can be children nodes or values

    @SuppressWarnings("unchecked")
    public BPlusTreeNode(int order, boolean isLeaf) {
        this.isLeaf = isLeaf;
        this.keyCount = 0;
        this.keys = (K[]) new Comparable[order - 1];
        if (isLeaf) {
            this.pointers = new Object[order]; // pointers[0] is for sibling, rest for values
        } else {
            this.pointers = new BPlusTreeNode[order];
        }
    }

    // Utility to find the index of a key or where it should be inserted
    public int findIndex(K key) {
        int index = Arrays.binarySearch(keys, 0, keyCount, key);
        return index >= 0 ? index : -index - 1;
    }
}
