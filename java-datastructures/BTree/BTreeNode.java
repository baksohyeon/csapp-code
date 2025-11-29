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

    // Find the index of the first key greater than or equal to k
    int findKey(int k) {
        int idx = 0;
        while (idx < n && keys[idx] < k) {
            idx++;
        }
        return idx;
    }

    // Remove the key at index idx from this node (leaf node)
    void removeFromLeaf(int idx) {
        for (int i = idx + 1; i < n; i++) {
            keys[i - 1] = keys[i];
        }
        n--;
    }

    // Remove the key at index idx from this node (internal node)
    void removeFromNonLeaf(int idx) {
        int k = keys[idx];

        if (children[idx].n >= t) {
            int pred = getPredecessor(idx);
            keys[idx] = pred;
            children[idx].remove(pred);
        } else if (children[idx + 1].n >= t) {
            int succ = getSuccessor(idx);
            keys[idx] = succ;
            children[idx + 1].remove(succ);
        } else {
            merge(idx);
            children[idx].remove(k);
        }
    }

    // Get predecessor key (rightmost key in left subtree)
    int getPredecessor(int idx) {
        BTreeNode cur = children[idx];
        while (!cur.leaf) {
            cur = cur.children[cur.n];
        }
        return cur.keys[cur.n - 1];
    }

    // Get successor key (leftmost key in right subtree)
    int getSuccessor(int idx) {
        BTreeNode cur = children[idx + 1];
        while (!cur.leaf) {
            cur = cur.children[0];
        }
        return cur.keys[0];
    }

    // Fill child at index idx which has fewer than t-1 keys
    void fill(int idx) {
        // If previous sibling has at least t keys, borrow from it
        if (idx != 0 && children[idx - 1].n >= t) {
            borrowFromPrev(idx);
        }
        // If next sibling has at least t keys, borrow from it
        else if (idx != n && children[idx + 1].n >= t) {
            borrowFromNext(idx);
        }
        // Merge with sibling
        else {
            if (idx != n) {
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    // Borrow a key from previous sibling
    void borrowFromPrev(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx - 1];

        // Move a key from parent to child
        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
        }

        // Move child pointers
        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        child.keys[0] = keys[idx - 1];

        if (!child.leaf) {
            child.children[0] = sibling.children[sibling.n];
        }

        keys[idx - 1] = sibling.keys[sibling.n - 1];

        child.n++;
        sibling.n--;
    }

    // Borrow a key from next sibling
    void borrowFromNext(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        child.keys[child.n] = keys[idx];

        if (!child.leaf) {
            child.children[child.n + 1] = sibling.children[0];
        }

        keys[idx] = sibling.keys[0];

        for (int i = 1; i < sibling.n; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
        }

        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        child.n++;
        sibling.n--;
    }

    // Merge child at idx with its sibling
    void merge(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];

        // Pull key from current node and merge with right sibling
        child.keys[t - 1] = keys[idx];

        // Copy keys from sibling to child
        for (int i = 0; i < sibling.n; i++) {
            child.keys[i + t] = sibling.keys[i];
        }

        // Copy child pointers
        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.children[i + t] = sibling.children[i];
            }
        }

        // Move keys in current node one step back
        for (int i = idx + 1; i < n; i++) {
            keys[i - 1] = keys[i];
        }

        // Move child pointers one step back
        for (int i = idx + 2; i <= n; i++) {
            children[i - 1] = children[i];
        }

        child.n += sibling.n + 1;
        n--;
    }

    // Main removal function
    void remove(int k) {
        int idx = findKey(k);

        if (idx < n && keys[idx] == k) {
            if (leaf) {
                removeFromLeaf(idx);
            } else {
                removeFromNonLeaf(idx);
            }
        } else {
            if (leaf) {
                System.out.println("The key " + k + " does not exist in the tree");
                return;
            }

            boolean flag = (idx == n);

            if (children[idx].n < t) {
                fill(idx);
            }

            if (flag && idx > n) {
                children[idx - 1].remove(k);
            } else {
                children[idx].remove(k);
            }
        }
    }
}
