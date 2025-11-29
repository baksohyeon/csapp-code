package BPlusTree;

public class BPlusTreeDemo {
    public static void main(String[] args) {
        System.out.println("B+ Tree Demo");
        
        // Create a B+ Tree with order 4
        // Each node can have at most 3 keys and 4 children/pointers.
        BPlusTree<Integer, String> bPlusTree = new BPlusTree<>(4);

        // The insert method is not implemented yet, so we'll just test the search.
        String result = bPlusTree.search(10);
        System.out.println("Searching for key 10...");
        if (result != null) {
            System.out.println("Found value: " + result);
        } else {
            System.out.println("Key 10 not found.");
        }

        bPlusTree.insert(10, "Value for 10");
        bPlusTree.printTree();
    }
}
