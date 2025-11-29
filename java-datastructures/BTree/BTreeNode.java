package BTree;

public class BTreeNode {
    int[] keys;
    int t; // Minimum degree
    BTreeNode[] children;
    int n; // Current number of keys
    boolean leaf;

    public BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
        this.keys = new int[2 * t - 1];
        this.children = new BTreeNode[2 * t];
        this.n = 0;
    }

    public void traverse() {
        int i;
        for (i = 0; i < this.n; i++) {
            if (!this.leaf) {
                children[i].traverse();
            }
            System.out.print(" " + keys[i]);
        }

        if (!leaf) {
            children[i].traverse();
        }
    }

    public BTreeNode search(int k) {
        int i = 0;
        while (i < n && k > keys[i]) {
            i++;
        }

        if (keys[i] == k) {
            return this;
        }

        if (leaf) {
            return null;
        }

        return children[i].search(k);
    }
}
