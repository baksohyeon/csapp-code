package BTree;

public class BTreeManualSetupDemo {
    public static void main(String[] args) {
        BTree bTree = new BTree(3); // 최소 차수 t=3인 B-Tree 생성

        // --- Manual B-Tree Construction ---
        // We will build the tree from the bottom up (Leaves first, then the Root)

        // 1. Create all the Leaf Nodes
        BTreeNode leaf1 = new BTreeNode(3, true);
        leaf1.n = 5;
        leaf1.keys[0] = 1; leaf1.keys[1] = 2; leaf1.keys[2] = 3; leaf1.keys[3] = 4; leaf1.keys[4] = 5;

        BTreeNode leaf2 = new BTreeNode(3, true);
        leaf2.n = 4;
        leaf2.keys[0] = 7; leaf2.keys[1] = 8; leaf2.keys[2] = 9; leaf2.keys[3] = 10;

        BTreeNode leaf3 = new BTreeNode(3, true);
        leaf3.n = 5;
        leaf3.keys[0] = 15; leaf3.keys[1] = 17; leaf3.keys[2] = 19; leaf3.keys[3] = 20; leaf3.keys[4] = 23;

        BTreeNode leaf4 = new BTreeNode(3, true);
        leaf4.n = 5;
        leaf4.keys[0] = 27; leaf4.keys[1] = 28; leaf4.keys[2] = 30; leaf4.keys[3] = 31; leaf4.keys[4] = 33;

        BTreeNode leaf5 = new BTreeNode(3, true);
        leaf5.n = 4;
        leaf5.keys[0] = 35; leaf5.keys[1] = 36; leaf5.keys[2] = 37; leaf5.keys[3] = 39;

        BTreeNode leaf6 = new BTreeNode(3, true);
        leaf6.n = 2;
        leaf6.keys[0] = 41; leaf6.keys[1] = 45;

        // 2. Create the Root Node
        BTreeNode root = new BTreeNode(3, false); // leaf is false
        root.n = 5;
        root.keys[0] = 6;
        root.keys[1] = 13;
        root.keys[2] = 25;
        root.keys[3] = 34;
        root.keys[4] = 40;

        // 3. Link the Leaf Nodes as children of the Root Node
        root.children[0] = leaf1;
        root.children[1] = leaf2;
        root.children[2] = leaf3;
        root.children[3] = leaf4;
        root.children[4] = leaf5;
        root.children[5] = leaf6;

        // 4. Set the manually created root to our B-Tree
        bTree.setRoot(root);

        // --- Verification ---
        System.out.println("B-Tree structure (Manually Set Up):");
        bTree.printTree();
        System.out.println();

        System.out.println("Traversal of the constructed B-Tree is:");
        bTree.traverse();
        System.out.println();
    }
}
