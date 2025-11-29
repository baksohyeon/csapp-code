package BTree;

public class BTreeDemo {
    public static void main(String[] args) {
        BTree bTree = new BTree(3); // 최소 차수 t=3인 B-Tree 생성

        // 사용자 제공 예시의 모든 키들을 삽입합니다.
        // 키는 정렬된 순서대로 삽입하여 트리의 변화를 관찰하기 용이하게 합니다.
        int[] keysToInsert = {
            6, 13, 25, 34, 40, 
            1, 2, 3, 4, 5, 
            7, 8, 9, 10, 
            15, 17, 19, 20, 23,
            27, 28, 30, 31, 33, 
            35, 36, 37, 38, 39, 
            41, 42, 435 
        };

        System.out.println("Inserting keys into the B-Tree:");
        for (int key : keysToInsert) {
            bTree.insert(key);
            System.out.print(key + " ");
        }
        System.out.println("\n");

        System.out.println("Traversal of the constructed B-Tree is:");
        bTree.traverse();
        System.out.println("\n");

        System.out.println("B-Tree structure:");
        bTree.printTree();
        System.out.println();


        System.out.println("Searching for keys:");
        int[] keysToSearch = {6, 15, 25, 40, 11, 22}; // 존재하는 키와 존재하지 않는 키 검색
        for (int key : keysToSearch) {
            if (bTree.search(key) != null) {
                System.out.println(key + " is found in the B-Tree.");
            } else {
                System.out.println(key + " is not found in the B-Tree.");
            }
        }
    }
}