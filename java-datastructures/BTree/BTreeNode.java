package BTree;

/**
 * B-Tree 노드 클래스
 *
 * 각 노드는 키 배열과 자식 포인터 배열을 가지며,
 * 삽입/삭제/검색의 실제 로직을 담당합니다.
 *
 * 노드 구조 (t=3 예시):
 *
 *     keys:     [k0, k1, k2, k3, k4]  (최대 2t-1 = 5개)
 *     children: [c0, c1, c2, c3, c4, c5]  (최대 2t = 6개)
 *
 *     관계: c0 < k0 < c1 < k1 < c2 < k2 < ...
 */
public class BTreeNode {
    int[] keys;           // 키 배열 (정렬된 상태 유지)
    int t;                // 최소 차수
    BTreeNode[] children; // 자식 노드 포인터 배열
    int n;                // 현재 저장된 키의 개수
    boolean leaf;         // 리프 노드 여부

    /**
     * 생성자
     * @param t 최소 차수
     * @param leaf 리프 노드 여부
     */
    public BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
        this.keys = new int[2 * t - 1];   // 최대 2t-1개 키
        this.children = new BTreeNode[2 * t];  // 최대 2t개 자식
        this.n = 0;
    }

    /**
     * 중위 순회 (In-order Traversal)
     *
     * 순서: 자식0 → 키0 → 자식1 → 키1 → ... → 키(n-1) → 자식n
     * 결과: 모든 키가 오름차순으로 출력됨
     */
    public void traverse() {
        int i;
        for (i = 0; i < this.n; i++) {
            // 내부 노드면 키 출력 전에 왼쪽 자식 먼저 순회
            if (!this.leaf) {
                children[i].traverse();
            }
            System.out.print(" " + keys[i]);
        }

        // 마지막 자식 순회 (키 개수보다 자식이 1개 더 많음)
        if (!leaf) {
            children[i].traverse();
        }
    }

    /**
     * 키 검색
     *
     * @param k 찾을 키
     * @return 키가 있는 노드, 없으면 null
     */
    public BTreeNode search(int k) {
        // k 이상인 첫 번째 키의 인덱스 찾기
        int i = 0;
        while (i < n && k > keys[i]) {
            i++;
        }

        // 찾았으면 현재 노드 반환
        if (keys[i] == k) {
            return this;
        }

        // 리프인데 못 찾았으면 없는 것
        if (leaf) {
            return null;
        }

        // 적절한 자식으로 재귀 검색
        return children[i].search(k);
    }

    // ==================== 삭제 관련 메서드 ====================

    /**
     * k 이상인 첫 번째 키의 인덱스를 찾음
     *
     * 예: keys = [5, 10, 15], k = 12
     *     → 반환값: 2 (keys[2]=15가 12 이상인 첫 번째 키)
     */
    int findKey(int k) {
        int idx = 0;
        while (idx < n && keys[idx] < k) {
            idx++;
        }
        return idx;
    }

    /**
     * 리프 노드에서 키 삭제 (가장 단순한 케이스)
     *
     * Before: [5, 10, 15], idx=1 (10 삭제)
     * After:  [5, 15]
     *
     * @param idx 삭제할 키의 인덱스
     */
    void removeFromLeaf(int idx) {
        // idx 이후의 키들을 한 칸씩 앞으로 이동
        for (int i = idx + 1; i < n; i++) {
            keys[i - 1] = keys[i];
        }
        n--;  // 키 개수 감소
    }

    /**
     * 내부 노드에서 키 삭제 (복잡한 케이스)
     *
     * 세 가지 전략:
     * 1. 왼쪽 자식에 키가 충분하면 → Predecessor로 대체
     * 2. 오른쪽 자식에 키가 충분하면 → Successor로 대체
     * 3. 둘 다 부족하면 → Merge 후 재귀 삭제
     *
     * @param idx 삭제할 키의 인덱스
     */
    void removeFromNonLeaf(int idx) {
        int k = keys[idx];  // 삭제할 키

        if (children[idx].n >= t) {
            // === Case 2a: 왼쪽 자식에 여유 있음 (≥ t개 키) ===
            // Predecessor(선행자)로 대체: 왼쪽 서브트리에서 가장 큰 키
            int pred = getPredecessor(idx);
            keys[idx] = pred;                  // 대체
            children[idx].remove(pred);        // 원본 삭제 (재귀)
        } else if (children[idx + 1].n >= t) {
            // === Case 2b: 오른쪽 자식에 여유 있음 (≥ t개 키) ===
            // Successor(후행자)로 대체: 오른쪽 서브트리에서 가장 작은 키
            int succ = getSuccessor(idx);
            keys[idx] = succ;                  // 대체
            children[idx + 1].remove(succ);   // 원본 삭제 (재귀)
        } else {
            // === Case 2c: 양쪽 자식 모두 최소 키만 보유 (t-1개) ===
            // 병합 후 병합된 자식에서 삭제
            merge(idx);
            children[idx].remove(k);  // merge 후 왼쪽 자식에서 삭제
        }
    }

    /**
     * Predecessor (선행자) 찾기
     *
     * keys[idx]의 왼쪽 서브트리에서 가장 큰 키
     * = 왼쪽 자식에서 시작해서 계속 오른쪽으로 내려간 끝
     *
     *       [idx 키]
     *        /
     *     [....]
     *          \
     *         [....]
     *              \
     *            [여기!]  ← 가장 오른쪽 리프의 마지막 키
     */
    int getPredecessor(int idx) {
        BTreeNode cur = children[idx];  // 왼쪽 자식부터 시작
        while (!cur.leaf) {
            cur = cur.children[cur.n];  // 계속 오른쪽으로
        }
        return cur.keys[cur.n - 1];  // 마지막 키가 predecessor
    }

    /**
     * Successor (후행자) 찾기
     *
     * keys[idx]의 오른쪽 서브트리에서 가장 작은 키
     * = 오른쪽 자식에서 시작해서 계속 왼쪽으로 내려간 끝
     *
     *       [idx 키]
     *              \
     *             [....]
     *              /
     *           [....]
     *            /
     *        [여기!]  ← 가장 왼쪽 리프의 첫 번째 키
     */
    int getSuccessor(int idx) {
        BTreeNode cur = children[idx + 1];  // 오른쪽 자식부터 시작
        while (!cur.leaf) {
            cur = cur.children[0];  // 계속 왼쪽으로
        }
        return cur.keys[0];  // 첫 번째 키가 successor
    }

    /**
     * 자식 노드 채우기 (Fill)
     *
     * children[idx]의 키가 부족할 때 (< t개) 호출
     * 형제에게서 빌려오거나, 불가능하면 병합
     *
     * @param idx 키가 부족한 자식의 인덱스
     */
    void fill(int idx) {
        if (idx != 0 && children[idx - 1].n >= t) {
            // 왼쪽 형제에게 여유 있음 → 빌려오기
            borrowFromPrev(idx);
        } else if (idx != n && children[idx + 1].n >= t) {
            // 오른쪽 형제에게 여유 있음 → 빌려오기
            borrowFromNext(idx);
        } else {
            // 양쪽 형제 모두 최소 키만 보유 → 병합
            if (idx != n) {
                merge(idx);      // 오른쪽 형제와 병합
            } else {
                merge(idx - 1);  // 마지막 자식이면 왼쪽 형제와 병합
            }
        }
    }

    /**
     * 왼쪽 형제에게서 키 빌려오기 (Left Rotation)
     *
     * Before:
     *        부모: [..., K, ...]
     *              /       \
     *    sibling: [a,b,c]  child: [x]  ← 키 부족!
     *
     * After:
     *        부모: [..., c, ...]   ← K가 child로, c가 부모로
     *              /       \
     *    sibling: [a,b]   child: [K,x]
     *
     * @param idx 키가 부족한 자식의 인덱스
     */
    void borrowFromPrev(int idx) {
        BTreeNode child = children[idx];      // 키가 부족한 자식
        BTreeNode sibling = children[idx - 1]; // 왼쪽 형제

        // 1. child의 키들을 오른쪽으로 한 칸씩 밀기
        for (int i = child.n - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
        }

        // 2. child의 자식 포인터도 밀기
        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }

        // 3. 부모의 키를 child의 첫 번째 키로 내림
        child.keys[0] = keys[idx - 1];

        // 4. sibling의 마지막 자식을 child의 첫 번째 자식으로 이동
        if (!child.leaf) {
            child.children[0] = sibling.children[sibling.n];
        }

        // 5. sibling의 마지막 키를 부모로 올림
        keys[idx - 1] = sibling.keys[sibling.n - 1];

        // 6. 키 개수 조정
        child.n++;
        sibling.n--;
    }

    /**
     * 오른쪽 형제에게서 키 빌려오기 (Right Rotation)
     *
     * Before:
     *        부모: [..., K, ...]
     *              /       \
     *      child: [x]     sibling: [a,b,c]
     *             ↑ 키 부족!
     *
     * After:
     *        부모: [..., a, ...]   ← K가 child로, a가 부모로
     *              /       \
     *      child: [x,K]   sibling: [b,c]
     *
     * @param idx 키가 부족한 자식의 인덱스
     */
    void borrowFromNext(int idx) {
        BTreeNode child = children[idx];      // 키가 부족한 자식
        BTreeNode sibling = children[idx + 1]; // 오른쪽 형제

        // 1. 부모의 키를 child의 마지막에 추가
        child.keys[child.n] = keys[idx];

        // 2. sibling의 첫 번째 자식을 child의 마지막 자식으로 이동
        if (!child.leaf) {
            child.children[child.n + 1] = sibling.children[0];
        }

        // 3. sibling의 첫 번째 키를 부모로 올림
        keys[idx] = sibling.keys[0];

        // 4. sibling의 키들을 왼쪽으로 한 칸씩 당기기
        for (int i = 1; i < sibling.n; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
        }

        // 5. sibling의 자식 포인터도 당기기
        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }

        // 6. 키 개수 조정
        child.n++;
        sibling.n--;
    }

    /**
     * 두 자식 노드 병합 (Merge)
     *
     * children[idx]와 children[idx+1]을 하나로 합침
     * 부모의 keys[idx]가 중간에 들어감
     *
     * Before (t=2):
     *        부모: [10, 20, 30]
     *              /   |   \   \
     *           [5]  [15] [25] [35]
     *               idx=1 ↑
     *
     * merge(1) 호출 시 (20을 내리고 [15]와 [25] 병합):
     *        부모: [10, 30]       ← 키 하나 줄어듦
     *              /   |   \
     *           [5] [15,20,25] [35]  ← 병합됨
     *
     * @param idx 병합할 왼쪽 자식의 인덱스
     */
    void merge(int idx) {
        BTreeNode child = children[idx];       // 왼쪽 자식
        BTreeNode sibling = children[idx + 1]; // 오른쪽 자식 (병합 대상)

        // 1. 부모의 키를 child의 중간 위치에 내림
        //    child는 현재 t-1개 키를 가지고 있으므로, t-1 인덱스에 삽입
        child.keys[t - 1] = keys[idx];

        // 2. sibling의 모든 키를 child로 복사
        for (int i = 0; i < sibling.n; i++) {
            child.keys[i + t] = sibling.keys[i];
        }

        // 3. sibling의 자식 포인터도 복사 (내부 노드인 경우)
        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.children[i + t] = sibling.children[i];
            }
        }

        // 4. 부모에서 keys[idx]를 제거 (키들을 왼쪽으로 한 칸씩)
        for (int i = idx + 1; i < n; i++) {
            keys[i - 1] = keys[i];
        }

        // 5. 부모에서 sibling 포인터 제거 (자식들을 왼쪽으로 한 칸씩)
        for (int i = idx + 2; i <= n; i++) {
            children[i - 1] = children[i];
        }

        // 6. 키 개수 조정
        child.n += sibling.n + 1;  // child: 기존 + 부모키(1) + sibling키
        n--;                        // 부모: 키 하나 줄어듦

        // sibling은 이제 garbage collected 됨
    }

    /**
     * 키 삭제 메인 함수 (재귀)
     *
     * 세 가지 상황:
     * 1. 현재 노드에 키가 있고, 리프 노드 → 바로 삭제
     * 2. 현재 노드에 키가 있고, 내부 노드 → 대체 후 재귀 삭제
     * 3. 현재 노드에 키가 없음 → 적절한 자식으로 내려가서 재귀 삭제
     *
     * @param k 삭제할 키
     */
    void remove(int k) {
        // k 이상인 첫 번째 키의 위치 찾기
        int idx = findKey(k);

        if (idx < n && keys[idx] == k) {
            // === 현재 노드에서 키를 찾음 ===
            if (leaf) {
                // Case 1: 리프 노드 → 단순 삭제
                removeFromLeaf(idx);
            } else {
                // Case 2: 내부 노드 → 복잡한 삭제
                removeFromNonLeaf(idx);
            }
        } else {
            // === 현재 노드에 키가 없음 → 자식으로 내려가야 함 ===
            if (leaf) {
                // 리프인데 못 찾았으면 키가 트리에 없는 것
                System.out.println("The key " + k + " does not exist in the tree");
                return;
            }

            // idx == n이면 마지막 자식으로 가야 함
            boolean flag = (idx == n);

            // 자식에 키가 부족하면 먼저 채우기 (선제적 보강)
            if (children[idx].n < t) {
                fill(idx);
            }

            // fill() 후 merge가 발생했을 수 있음
            // merge로 인해 인덱스가 바뀌었는지 확인
            if (flag && idx > n) {
                // merge로 자식 개수가 줄었으면 이전 자식으로
                children[idx - 1].remove(k);
            } else {
                children[idx].remove(k);
            }
        }
    }
}
