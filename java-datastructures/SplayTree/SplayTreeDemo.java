package SplayTree;

public class SplayTreeDemo {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("Splay Tree 데모");
        System.out.println("=".repeat(60));
        System.out.println();

        SplayTree tree = new SplayTree();

        // ===== 삽입 데모 =====
        System.out.println("--- 삽입 연산 ---");
        System.out.println();

        int[] keys = {50, 30, 70, 20, 40, 60, 80};
        System.out.println("삽입할 키: " + java.util.Arrays.toString(keys));
        System.out.println();

        for (int key : keys) {
            System.out.println(">> " + key + " 삽입");
            tree.insert(key);
            System.out.println("현재 루트: " + tree.getRootKey());
            System.out.println("중위 순회: ");
            tree.inorder();
            System.out.println();
        }

        System.out.println("최종 트리 구조:");
        tree.printTree();
        System.out.println();

        // ===== 탐색 데모 =====
        System.out.println("=".repeat(60));
        System.out.println("--- 탐색 연산 ---");
        System.out.println();

        int searchKey = 20;
        System.out.println(">> " + searchKey + " 탐색");
        boolean found = tree.search(searchKey);
        System.out.println("결과: " + (found ? "찾음!" : "없음"));
        System.out.println("탐색 후 루트: " + tree.getRootKey() + " (탐색한 노드가 루트로!)");
        System.out.println();

        System.out.println("현재 트리 구조:");
        tree.printTree();
        System.out.println();

        // ===== 연속 탐색 데모 (지역성) =====
        System.out.println("=".repeat(60));
        System.out.println("--- 연속 탐색 (지역성 효과) ---");
        System.out.println();

        System.out.println("60을 여러 번 탐색하면?");
        for (int i = 0; i < 3; i++) {
            tree.search(60);
            System.out.println((i + 1) + "번째 탐색 후 루트: " + tree.getRootKey());
        }
        System.out.println("→ 자주 접근하는 60이 계속 루트에 머물러 빠른 접근 가능!");
        System.out.println();

        // ===== 삭제 데모 =====
        System.out.println("=".repeat(60));
        System.out.println("--- 삭제 연산 ---");
        System.out.println();

        int deleteKey = 60;
        System.out.println(">> " + deleteKey + " 삭제");
        System.out.println("삭제 전 트리:");
        tree.printTree();
        System.out.println();

        boolean deleted = tree.delete(deleteKey);
        System.out.println("삭제 결과: " + (deleted ? "성공" : "실패"));
        System.out.println("삭제 후 루트: " + tree.getRootKey());
        System.out.println();

        System.out.println("삭제 후 트리:");
        tree.printTree();
        System.out.println();

        System.out.println("중위 순회 (정렬 순서 확인):");
        tree.inorder();
        System.out.println();

        // ===== 추가 삭제 테스트 =====
        System.out.println("=".repeat(60));
        System.out.println("--- 추가 삭제 테스트 ---");
        System.out.println();

        int[] deleteKeys = {20, 80};
        for (int key : deleteKeys) {
            System.out.println(">> " + key + " 삭제");
            tree.delete(key);
            System.out.println("현재 루트: " + tree.getRootKey());
            System.out.println("중위 순회: ");
            tree.inorder();
            System.out.println();
        }

        System.out.println("최종 트리 구조:");
        tree.printTree();
        System.out.println();

        // ===== 성능 특징 데모 =====
        System.out.println("=".repeat(60));
        System.out.println("--- Splay Tree의 특징 ---");
        System.out.println();
        System.out.println("1. 자가 조정: 균형 정보를 저장하지 않아도 자동으로 조정됨");
        System.out.println("2. 지역성: 최근 접근한 노드가 루트 근처에 위치");
        System.out.println("3. Amortized O(log n): 평균적으로 O(log n) 성능 보장");
        System.out.println("4. 캐시 친화적: 자주 쓰는 데이터가 상단에");
        System.out.println();
    }
}
