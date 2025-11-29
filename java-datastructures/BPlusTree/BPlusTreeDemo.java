package BPlusTree;

public class BPlusTreeDemo {
    public static void main(String[] args) {
        System.out.println("B+ Tree Demo");
        
        // Create a B+ Tree with order 4
        // Each node can have at most 3 keys and 4 children/pointers.
        BPlusTree<Integer, String> bPlusTree = new BPlusTree<>(4);

        // Print the initial empty tree
        System.out.println("\n--- Initial tree state ---");
        bPlusTree.printTree();
        
        // Test search on empty tree
        String result = bPlusTree.search(10);
        System.out.println("\nSearching for key 10 (should not be found): " + (result != null ? "Found" : "Not Found"));

        System.out.println("\n--- Inserting elements ---");

        // Insert elements that fit into a single leaf node first
        bPlusTree.insert(10, "Value10");
        bPlusTree.insert(20, "Value20");
        bPlusTree.insert(30, "Value30");
        System.out.println("\nTree state after inserting 10, 20, 30 (should be a single leaf node):");
        bPlusTree.printTree();
        System.out.println("Searching for 20: " + bPlusTree.search(20)); // Should find Value20

        // Insert an element that causes a leaf split (order 4, max 3 keys per node)
        // Keys: [10, 20, 30] -> Insert 40. Node becomes full [10, 20, 30, 40]
        // Split: Left: [10, 20], Right: [30, 40]. Promoted key: 30
        bPlusTree.insert(40, "Value40");
        System.out.println("\nTree state after inserting 40 (should cause a leaf split, root becomes internal):");
        bPlusTree.printTree();
        System.out.println("Searching for 10: " + bPlusTree.search(10));
        System.out.println("Searching for 30: " + bPlusTree.search(30));
        System.out.println("Searching for 40: " + bPlusTree.search(40));

        // Insert more elements to fill the existing leaf nodes and potentially cause another leaf split
        bPlusTree.insert(5, "Value5");
        System.out.println("\nTree state after inserting 5:");
        bPlusTree.printTree();
        System.out.println("Searching for 5: " + bPlusTree.search(5));

        bPlusTree.insert(25, "Value25");
        System.out.println("\nTree state after inserting 25 (should cause another leaf split):");
        bPlusTree.printTree();
        System.out.println("Searching for 25: " + bPlusTree.search(25));

        bPlusTree.insert(35, "Value35");
        System.out.println("\nTree state after inserting 35:");
        bPlusTree.printTree();
        System.out.println("Searching for 35: " + bPlusTree.search(35));

        bPlusTree.insert(15, "Value15");
        System.out.println("\nTree state after inserting 15 (should cause another leaf split):");
        bPlusTree.printTree();
        System.out.println("Searching for 15: " + bPlusTree.search(15));

        // Insert elements that cause internal node splits and root splits
        bPlusTree.insert(50, "Value50");
        System.out.println("\nTree state after inserting 50 (might cause internal node split):");
        bPlusTree.printTree();
        System.out.println("Searching for 50: " + bPlusTree.search(50));

        bPlusTree.insert(1, "Value1");
        System.out.println("\nTree state after inserting 1:");
        bPlusTree.printTree();
        System.out.println("Searching for 1: " + bPlusTree.search(1));

        bPlusTree.insert(2, "Value2");
        System.out.println("\nTree state after inserting 2:");
        bPlusTree.printTree();
        System.out.println("Searching for 2: " + bPlusTree.search(2));

        bPlusTree.insert(3, "Value3");
        System.out.println("\nTree state after inserting 3:");
        bPlusTree.printTree();
        System.out.println("Searching for 3: " + bPlusTree.search(3));

        bPlusTree.insert(4, "Value4");
        System.out.println("\nTree state after inserting 4 (might cause root split):");
        bPlusTree.printTree();
        System.out.println("Searching for 4: " + bPlusTree.search(4));

        // Test search for all inserted values
        System.out.println("\n--- Verifying all inserted values ---");
        int[] keysToSearch = {1, 2, 3, 4, 5, 10, 15, 20, 25, 30, 35, 40, 50};
        for (int k : keysToSearch) {
            System.out.println("Searching for " + k + ": " + bPlusTree.search(k));
        }

        System.out.println("\n--- Testing for non-existent keys ---");
        System.out.println("Searching for 99: " + bPlusTree.search(99)); // Should be null
        System.out.println("Searching for 0: " + bPlusTree.search(0));   // Should be null
    }
}
