package BTree;

public class BTreeDemo {
    public static void main(String[] args) {
        BTree bTree = new BTree(3); // A B-Tree with minimum degree 3

        bTree.insert(10);
        bTree.insert(20);
        bTree.insert(5);
        bTree.insert(6);
        bTree.insert(12);
        bTree.insert(30);
        bTree.insert(7);
        bTree.insert(17);

        System.out.println("Traversal of the constructed B-Tree is:");
        bTree.traverse();

        int keyToSearch = 6;
        if (bTree.search(keyToSearch) != null) {
            System.out.println(keyToSearch + " is found in the B-Tree.");
        } else {
            System.out.println(keyToSearch + " is not found in the B-Tree.");
        }

        keyToSearch = 15;
        if (bTree.search(keyToSearch) != null) {
            System.out.println(keyToSearch + " is found in the B-Tree.");
        } else {
            System.out.println(keyToSearch + " is not found in the B-Tree.");
        }
    }
}
