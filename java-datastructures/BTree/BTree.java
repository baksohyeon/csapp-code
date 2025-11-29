package BTree;

import java.util.LinkedList;
import java.util.Queue;

public class BTree {
    private BTreeNode root;
    private final int t; // Minimum degree

    public BTree(int t) {
        this.root = null;
        this.t = t;
    }

    // This method is for manually setting up a B-Tree structure.
    public void setRoot(BTreeNode root) {
        this.root = root;
    }

    // Method to print the B-Tree structure (Level-Order Traversal)
    public void printTree() {
        if (root == null) {
            System.out.println("The B-Tree is empty.");
            return;
        }

        Queue<BTreeNode> queue = new LinkedList<>();
        queue.add(root);

        int level = 0;
        while (!queue.isEmpty()) {
            int levelNodes = queue.size();
            System.out.print("Level " + level + ": ");
            while (levelNodes > 0) {
                BTreeNode currentNode = queue.poll();
                System.out.print("[");
                for (int i = 0; i < currentNode.n; i++) {
                    System.out.print(currentNode.keys[i] + (i == currentNode.n - 1 ? "" : ","));
                }
                System.out.print("] ");

                if (!currentNode.leaf) {
                    for (int i = 0; i <= currentNode.n; i++) {
                        if (currentNode.children[i] != null) {
                            queue.add(currentNode.children[i]);
                        }
                    }
                }
                levelNodes--;
            }
            System.out.println();
            level++;
        }
    }


    public void traverse() {
        if (root != null) {
            root.traverse();
        }
        System.out.println();
    }

    public BTreeNode search(int k) {
        return (root == null) ? null : root.search(k);
    }

    public void insert(int k) {
        if (root == null) {
            root = new BTreeNode(t, true);
            root.keys[0] = k;
            root.n = 1;
        } else {
            if (root.n == 2 * t - 1) {
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;
                splitChild(s, 0, root);
                int i = 0;
                if (s.keys[0] < k) {
                    i++;
                }
                insertNonFull(s.children[i], k);
                root = s;
            } else {
                insertNonFull(root, k);
            }
        }
    }

    private void insertNonFull(BTreeNode x, int k) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && x.keys[i] > k) {
                x.keys[i + 1] = x.keys[i];
                i--;
            }
            x.keys[i + 1] = k;
            x.n = x.n + 1;
        } else {
            while (i >= 0 && x.keys[i] > k) {
                i--;
            }
            if (x.children[i + 1].n == 2 * t - 1) {
                splitChild(x, i + 1, x.children[i + 1]);
                if (x.keys[i + 1] < k) {
                    i++;
                }
            }
            insertNonFull(x.children[i + 1], k);
        }
    }

    private void splitChild(BTreeNode x, int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.t, y.leaf);
        z.n = t - 1;
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];
        }
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        }
        y.n = t - 1;
        for (int j = x.n; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z;
        for (int j = x.n - 1; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
        }
        x.keys[i] = y.keys[t - 1];
        x.n = x.n + 1;
    }
}
