package BTree;

public class BTreeInsertionAnalysis {
    public static void main(String[] args) {
        // --- SETUP ---
        // 1. BTreeManualSetupDemo.java 와 동일하게 교재에 나온 B-Tree를 수동으로 구성합니다.
        BTree bTree = new BTree(3);
        BTreeNode leaf1 = new BTreeNode(3, true); leaf1.n = 5; leaf1.keys[0] = 1; leaf1.keys[1] = 2; leaf1.keys[2] = 3; leaf1.keys[3] = 4; leaf1.keys[4] = 5;
        BTreeNode leaf2 = new BTreeNode(3, true); leaf2.n = 4; leaf2.keys[0] = 7; leaf2.keys[1] = 8; leaf2.keys[2] = 9; leaf2.keys[3] = 10;
        BTreeNode leaf3 = new BTreeNode(3, true); leaf3.n = 5; leaf3.keys[0] = 15; leaf3.keys[1] = 17; leaf3.keys[2] = 19; leaf3.keys[3] = 20; leaf3.keys[4] = 23;
        BTreeNode leaf4 = new BTreeNode(3, true); leaf4.n = 5; leaf4.keys[0] = 27; leaf4.keys[1] = 28; leaf4.keys[2] = 30; leaf4.keys[3] = 31; leaf4.keys[4] = 33;
        BTreeNode leaf5 = new BTreeNode(3, true); leaf5.n = 4; leaf5.keys[0] = 35; leaf5.keys[1] = 36; leaf5.keys[2] = 37; leaf5.keys[3] = 39;
        BTreeNode leaf6 = new BTreeNode(3, true); leaf6.n = 2; leaf6.keys[0] = 41; leaf6.keys[1] = 45;
        BTreeNode root = new BTreeNode(3, false); root.n = 5; root.keys[0] = 6; root.keys[1] = 13; root.keys[2] = 25; root.keys[3] = 34; root.keys[4] = 40;
        root.children[0] = leaf1; root.children[1] = leaf2; root.children[2] = leaf3; root.children[3] = leaf4; root.children[4] = leaf5; root.children[5] = leaf6;
        bTree.setRoot(root);

        System.out.println("--- Initial B-Tree State ---");
        System.out.println("This is the structure from the textbook.");
        bTree.printTree();
        System.out.println("\n" + "=".repeat(50) + "\n");

        // --- ANALYSIS 1: INSERTING 18 ---
        System.out.println("--- Analysis: Inserting Key 18 ---");
        System.out.println("The key 18 should go into the leaf node [15,17,19,20,23]. Let's trace the insert logic.\n");

        System.out.println("Step 1: Check the Root Node.");
        System.out.println("The root is [6,13,25,34,40]. It is FULL (max keys for t=3 is 2*3-1=5).");
        System.out.println("ACTION: Before we can do anything else, we MUST split the root. This is a pre-emptive split.");
        System.out.println("  - A new empty root is created.");
        System.out.println("  - The old root's middle key, '25', is moved up to the new root.");
        System.out.println("  - The old root is split into two new nodes: [6,13] and [34,40]. These become children of the new root.");
        System.out.println("  - The tree height increases by 1.\n");

        System.out.println("Step 2: After Root Split, find the path for 18.");
        System.out.println("The tree now looks like: Root=[25], Children=[6,13] and [34,40].");
        System.out.println("To insert 18, we compare it with the new root's key '25'. Since 18 < 25, we must go down the LEFT path to the node [6,13].\n");

        System.out.println("Step 3: Check the next node in the path: [6,13].");
        System.out.println("This node is NOT full. So we find the correct child pointer for 18.");
        System.out.println("Since 18 > 13, we follow the pointer AFTER 13, which leads to the leaf node [15,17,19,20,23].\n");

        System.out.println("Step 4: Check the child node we are about to descend into: [15,17,19,20,23].");
        System.out.println("This leaf node is FULL.");
        System.out.println("ACTION: We must split this child node BEFORE descending into it.");
        System.out.println("  - Its middle key, '19', is moved up to its parent ([6,13]).");
        System.out.println("  - The parent [6,13] now becomes [6,13,19].");
        System.out.println("  - The leaf is split into two new leaves: [15,17] and [20,23].\n");

        System.out.println("Step 5: After Child Split, find the path for 18 again.");
        System.out.println("The parent node is now [6,13,19]. To insert 18, we find the correct path.");
        System.out.println("Since 13 < 18 < 19, we must descend to the leaf between 13 and 19, which is [15,17].\n");

        System.out.println("Step 6: Final insertion into a non-full leaf.");
        System.out.println("The leaf node [15,17] is NOT full. We can insert 18 directly.");
        System.out.println("ACTION: Insert 18 into [15,17], resulting in [15,17,18].\n");

        bTree.insert(18); // Execute the actual insertion
        System.out.println(">>> B-Tree structure after inserting 18:");
        bTree.printTree();
        System.out.println("\n" + "=".repeat(50) + "\n");

        // --- ANALYSIS 2: INSERTING 16 ---
        System.out.println("--- Analysis: Inserting Key 16 ---");
        System.out.println("This is a much simpler case.\n");

        System.out.println("Step 1: Start at the root [25]. 16 < 25, so go left to child [6,13,19].");
        System.out.println("Step 2: At node [6,13,19]. 13 < 16 < 19, so go to the child between 13 and 19, which is [15,17,18].");
        System.out.println("Step 3: The destination leaf [15,17,18] is NOT full.");
        System.out.println("ACTION: Insert 16 directly into the leaf, resulting in [15,16,17,18]. No splits are needed.\n");

        bTree.insert(16); // Execute the actual insertion
        System.out.println(">>> B-Tree structure after inserting 16:");
        bTree.printTree();
    }
}
