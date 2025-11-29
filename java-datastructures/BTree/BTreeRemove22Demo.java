package BTree;

/**
 * B-Tree 삭제(Deletion) 연산 상세 분석
 *
 * 이 프로그램은 B-Tree에서 키를 삭제하는 과정을 단계별로 분석합니다.
 * 특히, 내부 노드(Internal Node)에 있는 키를 삭제하는 복잡한 케이스를 다룹니다.
 *
 * B-Tree 삭제의 핵심 규칙:
 * 1. 리프 노드에서 삭제: 단순히 키를 제거
 * 2. 내부 노드에서 삭제:
 *    a) Predecessor(선행자) 또는 Successor(후행자)를 찾아 대체
 *    b) 대체한 후 리프 노드에서 재귀적으로 삭제
 * 3. 언더플로우(Underflow) 방지:
 *    - 각 노드는 최소 t-1개의 키를 유지해야 함 (루트 제외)
 *    - 부족하면 형제 노드에서 빌려오거나(borrow), 병합(merge)
 */
public class BTreeRemove22Demo {
    public static void main(String[] args) {
        // --- SETUP ---
        // B-Tree를 수동으로 구성합니다. 최소 차수(Minimum Degree) t=3
        // 이는 각 노드가 최소 2개(t-1), 최대 5개(2t-1)의 키를 가질 수 있음을 의미합니다.
        System.out.println("=".repeat(70));
        System.out.println("B-TREE 삭제 연산 분석: 키 22 삭제하기");
        System.out.println("=".repeat(70));
        System.out.println();

        BTree bTree = new BTree(3);

        System.out.println("--- B-Tree 구조 설명 ---");
        System.out.println("최소 차수 t = 3");
        System.out.println("  - 각 노드의 최소 키 개수: t-1 = 2개 (루트 제외)");
        System.out.println("  - 각 노드의 최대 키 개수: 2t-1 = 5개");
        System.out.println("  - 각 노드의 최대 자식 개수: 2t = 6개");
        System.out.println();

        // --- Manual B-Tree Construction ---
        // Tree structure:
        // level 0 (root):     [15]
        // level 1 (internal): [3, 8]                    [19, 22]
        // level 2 (leaves):   [1,2] [5,6] [9,10]        [16,18] [20,21] [24,25,26]

        System.out.println("--- 트리 구조 수동 생성 ---");
        System.out.println();

        // 1. Create all Leaf Nodes (Level 2)
        System.out.println("1단계: 리프 노드(Leaf Nodes) 생성 (Level 2)");

        BTreeNode leaf1 = new BTreeNode(3, true);
        leaf1.n = 2;
        leaf1.keys[0] = 1;
        leaf1.keys[1] = 2;
        System.out.println("  - leaf1: [1, 2]");

        BTreeNode leaf2 = new BTreeNode(3, true);
        leaf2.n = 2;
        leaf2.keys[0] = 5;
        leaf2.keys[1] = 6;
        System.out.println("  - leaf2: [5, 6]");

        BTreeNode leaf3 = new BTreeNode(3, true);
        leaf3.n = 2;
        leaf3.keys[0] = 9;
        leaf3.keys[1] = 10;
        System.out.println("  - leaf3: [9, 10]");

        BTreeNode leaf4 = new BTreeNode(3, true);
        leaf4.n = 2;
        leaf4.keys[0] = 16;
        leaf4.keys[1] = 18;
        System.out.println("  - leaf4: [16, 18]");

        BTreeNode leaf5 = new BTreeNode(3, true);
        leaf5.n = 2;
        leaf5.keys[0] = 20;
        leaf5.keys[1] = 21;
        System.out.println("  - leaf5: [20, 21] ← 여기서 Predecessor(21)를 가져올 예정");

        BTreeNode leaf6 = new BTreeNode(3, true);
        leaf6.n = 3;
        leaf6.keys[0] = 24;
        leaf6.keys[1] = 25;
        leaf6.keys[2] = 26;
        System.out.println("  - leaf6: [24, 25, 26]");
        System.out.println();

        // 2. Create Internal Nodes (Level 1)
        System.out.println("2단계: 내부 노드(Internal Nodes) 생성 (Level 1)");

        BTreeNode internal1 = new BTreeNode(3, false);
        internal1.n = 2;
        internal1.keys[0] = 3;
        internal1.keys[1] = 8;
        internal1.children[0] = leaf1;
        internal1.children[1] = leaf2;
        internal1.children[2] = leaf3;
        System.out.println("  - internal1: [3, 8]");
        System.out.println("    자식들: [1,2], [5,6], [9,10]");

        BTreeNode internal2 = new BTreeNode(3, false);
        internal2.n = 2;
        internal2.keys[0] = 19;
        internal2.keys[1] = 22;  // ← 삭제할 키!
        internal2.children[0] = leaf4;
        internal2.children[1] = leaf5;
        internal2.children[2] = leaf6;
        System.out.println("  - internal2: [19, 22] ← 여기에 삭제할 키 22가 있습니다!");
        System.out.println("    자식들: [16,18], [20,21], [24,25,26]");
        System.out.println();

        // 3. Create Root Node (Level 0)
        System.out.println("3단계: 루트 노드(Root Node) 생성 (Level 0)");

        BTreeNode root = new BTreeNode(3, false);
        root.n = 1;
        root.keys[0] = 15;
        root.children[0] = internal1;
        root.children[1] = internal2;
        System.out.println("  - root: [15]");
        System.out.println("    자식들: [3,8], [19,22]");
        System.out.println();

        // 4. Set the root
        bTree.setRoot(root);

        // --- Verification ---
        System.out.println("=".repeat(70));
        System.out.println("초기 B-Tree 구조:");
        System.out.println("=".repeat(70));
        bTree.printTree();
        System.out.println();

        System.out.println("중위 순회(In-order Traversal) 결과:");
        bTree.traverse();
        System.out.println();
        System.out.println();

        // --- Deletion Analysis ---
        System.out.println("=".repeat(70));
        System.out.println("삭제 연산 분석: 키 22 삭제");
        System.out.println("=".repeat(70));
        System.out.println();

        System.out.println("--- 문제 상황 ---");
        System.out.println("삭제할 키: 22");
        System.out.println("위치: 내부 노드 [19, 22]의 두 번째 키");
        System.out.println("문제: 내부 노드에서 키를 삭제하는 것은 리프 노드보다 복잡합니다!");
        System.out.println();

        System.out.println("--- B-Tree 삭제 알고리즘 (내부 노드 케이스) ---");
        System.out.println();

        System.out.println("Step 1: 삭제할 키 22를 찾습니다.");
        System.out.println("  루트 [15]에서 시작 → 22 > 15이므로 오른쪽 자식으로");
        System.out.println("  내부 노드 [19, 22]에 도착 → 22를 찾았습니다! (인덱스 1)");
        System.out.println();

        System.out.println("Step 2: 내부 노드에서 삭제 - 세 가지 전략 중 선택");
        System.out.println("  전략 A: 왼쪽 자식이 충분한 키를 가지고 있다면 (≥ t개)");
        System.out.println("          → Predecessor(선행자)를 찾아 22를 대체하고, 선행자를 재귀적으로 삭제");
        System.out.println("  전략 B: 오른쪽 자식이 충분한 키를 가지고 있다면 (≥ t개)");
        System.out.println("          → Successor(후행자)를 찾아 22를 대체하고, 후행자를 재귀적으로 삭제");
        System.out.println("  전략 C: 양쪽 자식 모두 최소 키만 가지고 있다면 (= t-1개)");
        System.out.println("          → 두 자식을 병합(merge)한 후 재귀적으로 삭제");
        System.out.println();

        System.out.println("Step 3: 현재 상황 분석");
        System.out.println("  - 22의 왼쪽 자식: [20, 21] → 키 개수 = 2");
        System.out.println("  - 22의 오른쪽 자식: [24, 25, 26] → 키 개수 = 3");
        System.out.println("  - 최소 차수 t = 3이므로:");
        System.out.println("    * 왼쪽 자식: 2 < 3 ✗ (부족함!)");
        System.out.println("    * 오른쪽 자식: 3 ≥ 3 ✓ (충분함)");
        System.out.println();

        System.out.println("Step 4: 전략 선택");
        System.out.println("  - 왼쪽 자식이 부족하므로 전략 A는 사용 불가 ✗");
        System.out.println("  - 오른쪽 자식이 충분하므로 전략 B를 사용! ✓");
        System.out.println("  → Successor(후행자)를 사용하여 22를 대체합니다.");
        System.out.println();

        System.out.println("Step 5: Successor(후행자) 찾기");
        System.out.println("  - Successor란? 22보다 큰 키들 중 가장 작은 키");
        System.out.println("  - 찾는 방법: 22의 오른쪽 자식에서 시작하여");
        System.out.println("              왼쪽으로 계속 내려가며 가장 왼쪽 리프의 첫 번째 키를 찾음");
        System.out.println("  - 22는 internal2의 인덱스 1에 위치");
        System.out.println("  - 오른쪽 자식 = children[2] = [24, 25, 26] (리프 노드)");
        System.out.println("  - 이미 리프이므로 첫 번째 키: Successor = 24");
        System.out.println();

        System.out.println("Step 6: 키 대체 및 재귀 삭제");
        System.out.println("  ACTION 1: 내부 노드 [19, 22]에서 22를 24로 대체");
        System.out.println("            → [19, 22] becomes [19, 24]");
        System.out.println();
        System.out.println("  ACTION 2: 리프 노드 [24, 25, 26]에서 24를 재귀적으로 삭제");
        System.out.println("            → [24, 25, 26] becomes [25, 26]");
        System.out.println();
        System.out.println("Step 7: 삭제 완료 후 B-Tree 속성 확인");
        System.out.println("  - 모든 리프 노드는 같은 레벨에 있는가? ✓");
        System.out.println("  - 각 노드가 최소 키 개수를 만족하는가?");
        System.out.println("    * 루트: 1개 (최소 제한 없음) ✓");
        System.out.println("    * 내부 노드들: 2개 ≥ t-1=2 ✓");
        System.out.println("    * 모든 리프: 2개 이상 ≥ t-1=2 ✓");
        System.out.println();

        System.out.println(">>> 실제 삭제 실행 중...");
        bTree.remove(22);
        System.out.println();

        System.out.println("=".repeat(70));
        System.out.println("22 삭제 후 B-Tree 구조:");
        System.out.println("=".repeat(70));
        bTree.printTree();

        System.out.println(">>> 실제 삭제 실행 중...");
        bTree.remove(19);
        System.out.println();

        System.out.println("=".repeat(70));
        System.out.println("19 삭제 후 B-Tree 구조:");
        System.out.println("=".repeat(70));
        bTree.printTree();
        System.out.println();

        System.out.println();
    }
}
