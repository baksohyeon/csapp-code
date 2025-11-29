package SplayTree;

/**
 * Splay Tree Implementation
 *
 * Splay Tree는 자가 조정(self-adjusting) 이진 탐색 트리다.
 * 접근한 노드를 루트로 이동시키는 splay 연산을 통해
 * 자주 접근하는 노드를 트리 상단에 위치시킨다.
 *
 * 특징:
 * - 균형 정보를 저장하지 않음 (AVL, Red-Black과 다름)
 * - 모든 연산 후 splay 수행
 * - Amortized O(log n) 시간 복잡도
 * - 지역성(locality) 있는 접근 패턴에서 매우 효율적
 */
public class SplayTree {
    private Node root;

    // Splay Tree Node
    static class Node {
        int key;
        Node left, right, parent;

        Node(int key) {
            this.key = key;
            this.left = null;
            this.right = null;
            this.parent = null;
        }
    }

    public SplayTree() {
        this.root = null;
    }

    // ========== Rotation Operations ==========

    /**
     * Right Rotation (Zig)
     *
     *       y                x
     *      / \              / \
     *     x   C    -->     A   y
     *    / \                  / \
     *   A   B                B   C
     */
    private void rightRotate(Node x) {
        Node y = x.parent;
        if (y == null) return;

        x.parent = y.parent;
        if (y.parent != null) {
            if (y.parent.left == y) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
        }

        y.left = x.right;
        if (x.right != null) {
            x.right.parent = y;
        }

        x.right = y;
        y.parent = x;

        if (y == root) {
            root = x;
        }
    }

    /**
     * Left Rotation (Zig)
     *
     *     x                  y
     *    / \                / \
     *   A   y      -->     x   C
     *      / \            / \
     *     B   C          A   B
     */
    private void leftRotate(Node x) {
        Node y = x.parent;
        if (y == null) return;

        x.parent = y.parent;
        if (y.parent != null) {
            if (y.parent.left == y) {
                y.parent.left = x;
            } else {
                y.parent.right = x;
            }
        }

        y.right = x.left;
        if (x.left != null) {
            x.left.parent = y;
        }

        x.left = y;
        y.parent = x;

        if (y == root) {
            root = x;
        }
    }

    // ========== Splay Operation ==========

    /**
     * Splay 연산: 노드 x를 루트로 이동
     *
     * 세 가지 케이스:
     * 1. Zig: x의 부모가 루트인 경우 (한 번만 회전)
     * 2. Zig-Zig: x, parent, grandparent가 일직선
     * 3. Zig-Zag: x, parent, grandparent가 꺾인 형태
     */
    private void splay(Node x) {
        if (x == null) return;

        while (x.parent != null) {
            Node parent = x.parent;
            Node grandparent = parent.parent;

            if (grandparent == null) {
                // Case 1: Zig (부모가 루트)
                if (parent.left == x) {
                    rightRotate(x);
                } else {
                    leftRotate(x);
                }
            } else if (grandparent.left == parent && parent.left == x) {
                // Case 2: Zig-Zig (Left-Left)
                //       G
                //      /
                //     P
                //    /
                //   X
                rightRotate(parent);
                rightRotate(x);
            } else if (grandparent.right == parent && parent.right == x) {
                // Case 2: Zig-Zig (Right-Right)
                //   G
                //    \
                //     P
                //      \
                //       X
                leftRotate(parent);
                leftRotate(x);
            } else if (grandparent.left == parent && parent.right == x) {
                // Case 3: Zig-Zag (Left-Right)
                //     G
                //    /
                //   P
                //    \
                //     X
                leftRotate(x);
                rightRotate(x);
            } else {
                // Case 3: Zig-Zag (Right-Left)
                //   G
                //    \
                //     P
                //    /
                //   X
                rightRotate(x);
                leftRotate(x);
            }
        }

        root = x;
    }

    // ========== Search Operation ==========

    /**
     * 탐색: 키를 찾고 해당 노드를 루트로 splay
     *
     * @param key 찾을 키
     * @return 찾았으면 true, 아니면 false
     */
    public boolean search(int key) {
        Node current = root;
        Node lastVisited = null;

        while (current != null) {
            lastVisited = current;

            if (key == current.key) {
                // 찾았음! 이 노드를 루트로 splay
                splay(current);
                return true;
            } else if (key < current.key) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // 못 찾았지만, 마지막으로 방문한 노드를 splay
        // (가장 가까운 노드를 루트로 올림)
        if (lastVisited != null) {
            splay(lastVisited);
        }

        return false;
    }

    // ========== Insert Operation ==========

    /**
     * 삽입: 새 노드를 삽입하고 루트로 splay
     *
     * @param key 삽입할 키
     */
    public void insert(int key) {
        if (root == null) {
            root = new Node(key);
            return;
        }

        Node current = root;
        Node parent = null;

        // BST 삽입 위치 찾기
        while (current != null) {
            parent = current;

            if (key == current.key) {
                // 중복 키는 splay만 하고 종료
                splay(current);
                return;
            } else if (key < current.key) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // 새 노드 생성 및 연결
        Node newNode = new Node(key);
        newNode.parent = parent;

        if (key < parent.key) {
            parent.left = newNode;
        } else {
            parent.right = newNode;
        }

        // 새 노드를 루트로 splay
        splay(newNode);
    }

    // ========== Delete Operation ==========

    /**
     * 삭제: 노드를 찾아 삭제하고 트리를 재구성
     *
     * 삭제 알고리즘:
     * 1. 삭제할 노드를 루트로 splay
     * 2. 루트를 제거하면 왼쪽 서브트리 L과 오른쪽 서브트리 R로 분리
     * 3. L이 없으면 R을 새 루트로
     * 4. L이 있으면:
     *    a) L에서 최댓값을 루트로 splay (이 노드는 오른쪽 자식이 없음)
     *    b) R을 새 루트의 오른쪽 자식으로 연결
     *
     * @param key 삭제할 키
     * @return 삭제 성공 여부
     */
    public boolean delete(int key) {
        if (root == null) {
            return false;
        }

        // 삭제할 노드를 루트로 splay
        if (!search(key)) {
            // 노드가 없음 (search가 이미 가장 가까운 노드를 splay함)
            return false;
        }

        // 이제 root가 삭제할 노드임
        Node leftSubtree = root.left;
        Node rightSubtree = root.right;

        if (leftSubtree == null) {
            // 왼쪽 서브트리가 없으면 오른쪽을 새 루트로
            root = rightSubtree;
            if (root != null) {
                root.parent = null;
            }
        } else {
            // 왼쪽 서브트리에서 최댓값 찾기
            Node maxNode = leftSubtree;
            while (maxNode.right != null) {
                maxNode = maxNode.right;
            }

            // 왼쪽 서브트리를 임시 루트로 설정
            root = leftSubtree;
            root.parent = null;

            // 최댓값을 루트로 splay
            splay(maxNode);

            // 이제 root는 maxNode이고, root.right는 null임
            // 오른쪽 서브트리를 연결
            root.right = rightSubtree;
            if (rightSubtree != null) {
                rightSubtree.parent = root;
            }
        }

        return true;
    }

    // ========== Helper Methods ==========

    /**
     * 중위 순회 (In-order Traversal)
     */
    public void inorder() {
        inorderHelper(root);
        System.out.println();
    }

    private void inorderHelper(Node node) {
        if (node != null) {
            inorderHelper(node.left);
            System.out.print(node.key + " ");
            inorderHelper(node.right);
        }
    }

    /**
     * 트리 구조 출력 (레벨별)
     */
    public void printTree() {
        if (root == null) {
            System.out.println("Tree is empty");
            return;
        }

        printTreeHelper(root, "", true);
    }

    private void printTreeHelper(Node node, String prefix, boolean isTail) {
        if (node == null) return;

        System.out.println(prefix + (isTail ? "└── " : "├── ") + node.key);

        if (node.left != null || node.right != null) {
            if (node.right != null) {
                printTreeHelper(node.right, prefix + (isTail ? "    " : "│   "), false);
            }
            if (node.left != null) {
                printTreeHelper(node.left, prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }

    /**
     * 루트 노드의 키 반환
     */
    public Integer getRootKey() {
        return root != null ? root.key : null;
    }

    /**
     * 트리가 비어있는지 확인
     */
    public boolean isEmpty() {
        return root == null;
    }
}
