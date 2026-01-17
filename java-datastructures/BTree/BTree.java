package BTree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * B-Tree 클래스
 *
 * B-Tree는 자기 균형 트리로, 디스크 기반 스토리지에 최적화된 자료구조입니다.
 * 이 클래스는 트리 전체를 관리하고, 외부에 insert/remove/search 인터페이스를 제공합니다.
 */
public class BTree {
    private BTreeNode root;  // 트리의 루트 노드
    private final int t;     // 최소 차수 (Minimum degree)
                             // - 각 노드는 최소 t-1개, 최대 2t-1개의 키를 가짐
                             // - 각 내부 노드는 최소 t개, 최대 2t개의 자식을 가짐

    /**
     * 생성자: 빈 B-Tree를 생성
     * @param t 최소 차수 (t >= 2)
     */
    public BTree(int t) {
        this.root = null;
        this.t = t;
    }

    /**
     * 테스트/학습 목적으로 루트를 직접 설정하는 메서드
     */
    public void setRoot(BTreeNode root) {
        this.root = root;
    }

    /**
     * 트리 구조를 레벨별로 출력 (BFS 방식)
     *
     * 출력 예시 (t=2):
     * Level 0: [10]
     * Level 1: [5] [15,20]
     */
    public void printTree() {
        if (root == null) {
            System.out.println("The B-Tree is empty.");
            return;
        }

        // BFS를 위한 큐 사용
        Queue<BTreeNode> queue = new LinkedList<>();
        queue.add(root);

        int level = 0;
        while (!queue.isEmpty()) {
            int levelNodes = queue.size();  // 현재 레벨의 노드 개수
            System.out.print("Level " + level + ": ");

            // 현재 레벨의 모든 노드 출력
            while (levelNodes > 0) {
                BTreeNode currentNode = queue.poll();

                // 노드의 키들을 [k1,k2,k3] 형태로 출력
                System.out.print("[");
                for (int i = 0; i < currentNode.n; i++) {
                    System.out.print(currentNode.keys[i] + (i == currentNode.n - 1 ? "" : ","));
                }
                System.out.print("] ");

                // 내부 노드면 자식들을 큐에 추가 (다음 레벨 탐색용)
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

    /**
     * 중위 순회 (In-order Traversal)
     * 결과: 키들이 오름차순으로 출력됨
     */
    public void traverse() {
        if (root != null) {
            root.traverse();
        }
        System.out.println();
    }

    /**
     * 키 검색
     * @param k 찾을 키
     * @return 키가 있는 노드, 없으면 null
     */
    public BTreeNode search(int k) {
        return (root == null) ? null : root.search(k);
    }

    /**
     * 키 삽입
     *
     * 핵심 전략: "선제적 분할 (Pre-emptive Split)"
     * - 내려가기 전에 꽉 찬 노드를 미리 분할
     * - 이렇게 하면 삽입 후 다시 올라올 필요 없음 (backtracking 불필요)
     *
     * @param k 삽입할 키
     */
    public void insert(int k) {
        // Case 1: 트리가 비어있으면 새 루트 생성
        if (root == null) {
            root = new BTreeNode(t, true);  // 리프 노드로 생성
            root.keys[0] = k;
            root.n = 1;
        } else {
            // Case 2: 루트가 꽉 찼으면 (2t-1개의 키) 먼저 분할
            if (root.n == 2 * t - 1) {
                // 새로운 루트 생성 (비어있는 내부 노드)
                BTreeNode s = new BTreeNode(t, false);
                s.children[0] = root;  // 기존 루트를 자식으로

                // 기존 루트를 분할 → 트리 높이 1 증가
                splitChild(s, 0, root);

                // 새 키가 어느 자식으로 가야 하는지 결정
                int i = 0;
                if (s.keys[0] < k) {
                    i++;  // 오른쪽 자식으로
                }
                insertNonFull(s.children[i], k);

                root = s;  // 새 루트로 교체
            } else {
                // Case 3: 루트에 여유 있으면 바로 삽입
                insertNonFull(root, k);
            }
        }
    }

    /**
     * 꽉 차지 않은 노드에 키 삽입 (재귀)
     *
     * 전제: x는 꽉 차있지 않음 (n < 2t-1)
     *
     * @param x 삽입할 노드
     * @param k 삽입할 키
     */
    private void insertNonFull(BTreeNode x, int k) {
        int i = x.n - 1;  // 마지막 키의 인덱스

        if (x.leaf) {
            // === 리프 노드: 직접 삽입 ===
            // 정렬 순서를 유지하면서 키를 삽입할 위치 찾기
            // (뒤에서부터 비교하며 한 칸씩 밀기)
            while (i >= 0 && x.keys[i] > k) {
                x.keys[i + 1] = x.keys[i];  // 오른쪽으로 한 칸 이동
                i--;
            }
            x.keys[i + 1] = k;  // 올바른 위치에 삽입
            x.n = x.n + 1;
        } else {
            // === 내부 노드: 적절한 자식으로 내려가기 ===
            // k가 들어갈 자식 찾기
            while (i >= 0 && x.keys[i] > k) {
                i--;
            }
            // children[i+1]이 k가 속할 자식

            // 선제적 분할: 자식이 꽉 찼으면 미리 분할!
            if (x.children[i + 1].n == 2 * t - 1) {
                splitChild(x, i + 1, x.children[i + 1]);

                // 분할 후 새로 올라온 키와 비교해서 방향 결정
                if (x.keys[i + 1] < k) {
                    i++;  // 오른쪽 자식으로
                }
            }
            // 재귀적으로 자식에 삽입
            insertNonFull(x.children[i + 1], k);
        }
    }

    /**
     * 자식 노드 분할 (Split)
     *
     * 꽉 찬 자식 y를 둘로 나누고, 중간 키를 부모 x로 올림
     *
     * Before (y가 꽉 참, t=3):
     *        x: [..., _, ...]
     *             |
     *        y: [1,2,3,4,5]  (2t-1 = 5개)
     *
     * After:
     *        x: [..., 3, ...]   ← 중간키(3)가 올라옴
     *            /     \
     *      y: [1,2]   z: [4,5]  ← 좌우로 분할
     *
     * @param x 부모 노드
     * @param i y가 x의 몇 번째 자식인지
     * @param y 분할할 자식 노드 (꽉 찬 상태)
     */
    private void splitChild(BTreeNode x, int i, BTreeNode y) {
        // 1. 새 노드 z 생성 (y의 오른쪽 절반을 가져갈 노드)
        BTreeNode z = new BTreeNode(y.t, y.leaf);
        z.n = t - 1;  // 오른쪽 절반의 키 개수

        // 2. y의 오른쪽 절반 키들을 z로 복사
        //    y: [k0, k1, ..., k(t-2), k(t-1), k(t), ..., k(2t-2)]
        //                              ↑중간키    └──── z로 이동 ────┘
        for (int j = 0; j < t - 1; j++) {
            z.keys[j] = y.keys[j + t];  // k(t) ~ k(2t-2)
        }

        // 3. y가 내부 노드면 자식 포인터도 복사
        if (!y.leaf) {
            for (int j = 0; j < t; j++) {
                z.children[j] = y.children[j + t];
            }
        }

        // 4. y의 키 개수 조정 (왼쪽 절반만 남김)
        y.n = t - 1;

        // 5. 부모 x에서 z를 위한 공간 확보 (자식 포인터 밀기)
        for (int j = x.n; j >= i + 1; j--) {
            x.children[j + 1] = x.children[j];
        }
        x.children[i + 1] = z;  // z를 x의 자식으로 연결

        // 6. 부모 x에서 중간 키를 위한 공간 확보 (키 밀기)
        for (int j = x.n - 1; j >= i; j--) {
            x.keys[j + 1] = x.keys[j];
        }

        // 7. 중간 키를 부모로 올림
        x.keys[i] = y.keys[t - 1];  // k(t-1)이 중간 키
        x.n = x.n + 1;
    }

    /**
     * 키 삭제
     *
     * 노드 단위 삭제는 BTreeNode.remove()가 처리하고,
     * 이 메서드는 트리 단위 관리 (루트 처리)를 담당
     *
     * @param k 삭제할 키
     */
    public void remove(int k) {
        if (root == null) {
            System.out.println("The tree is empty");
            return;
        }

        // 노드 단위 삭제 실행
        root.remove(k);

        // === 트리 단위 관리: 루트가 비었는지 확인 ===
        // merge로 인해 루트의 키가 모두 사라질 수 있음
        if (root.n == 0) {
            if (!root.leaf && root.children[0] != null) {
                // 루트의 유일한 자식이 새 루트가 됨 → 트리 높이 감소
                root = root.children[0];
            } else {
                // 트리가 완전히 비었음
                root = null;
            }
        }
    }
}
