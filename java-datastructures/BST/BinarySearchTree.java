package BST;

public class BinarySearchTree<T extends Comparable<T>> implements IndexInterface<T, TreeNode<T>> {
	private TreeNode<T> root;
	public BinarySearchTree() { // 생성자
		root = null;
	}
	
	// [알고리즘 10-1] 구현 : 검색
	public TreeNode<T> search(T searchKey) {
		return searchItem(root, searchKey);
	}
	
	private TreeNode<T> searchItem(TreeNode<T> tNode, T searchKey) {
		if (tNode == null) 
			return null;  // 검색 실패
		else if (searchKey.compareTo(tNode.key) == 0)
			return tNode;
		else if (searchKey.compareTo(tNode.key) < 0)
			return searchItem(tNode.left, searchKey);
		else 
			return searchItem(tNode.right, searchKey);
	}
	
	// [알고리즘 10-3] 구현 : 삽입
	public void insert(T newKey) {
		root = insertItem(root, newKey);
	}
 
	private TreeNode<T> insertItem(TreeNode<T> tNode, T newItem) {
		if (tNode == null)  // insert after a leaf  (or into an empty tree)
			tNode = new TreeNode<>(newItem, null, null);
		else  if (newItem.compareTo(tNode.key) < 0) 	// branch left
			tNode.left = insertItem(tNode.left, newItem);
		else  					// branch right
			tNode.right = insertItem(tNode.right, newItem);
		return tNode; 
	}
	
	// [알고리즘 10-3] 구현 : 삭제
	public void delete(T searchKey) {
		root = deleteItem(root, searchKey);
	}
	
	private TreeNode<T> deleteItem(TreeNode<T> tNode, T searchKey) {
		if (tNode == null) return null;	// key not found!
		else {
			if (searchKey.compareTo(tNode.key) == 0) 	// key found at tNode
				tNode = deleteNode(tNode);
			else if (searchKey.compareTo(tNode.key) < 0) 
				tNode.left = deleteItem(tNode.left, searchKey);
			else 
				tNode.right = deleteItem(tNode.right, searchKey);
			return tNode;
		}
	}
 
	private TreeNode<T> deleteNode(TreeNode<T> tNode) {
	// 3가지 case
		//    1. tNode이 리프 노드
		//    2. tNode이 자식이 하나만 있음
		//    3. tNode이 자식이 둘 있음
		if ((tNode.left == null) && (tNode.right == null))  // case 1(자식이 없음)
			return null;  
		else if (tNode.left == null) 	// case 2(오른자식뿐)
			return tNode.right;
      	else if (tNode.right == null)   	// case 2(왼자식뿐)
       		return tNode.left;	
       else {	 	 	 	 		// case 3(두 자식이 다 있음)
        		returnPair rPair = deleteMinItem(tNode.right);
        		tNode.key = rPair.key; tNode.right = rPair.node;
        		return tNode; 
        	}
	}
	
	private returnPair deleteMinItem(TreeNode<T> tNode) {
		if (tNode.left == null) 
			return new returnPair(tNode.key,tNode.right);
		else {
			returnPair rPair = deleteMinItem(tNode.left); 
			tNode.left = rPair.node;
			rPair.node = tNode;
			return rPair;
		}
	}
	
	private class returnPair {
		private T key;
		private TreeNode<T> node;
		private returnPair(T it, TreeNode<T> nd) {
			key = it;
			node = nd;
		}
	}
	
	// 기타
	public boolean isEmpty() {
		return root == null;
	}
	
	public void clear() {
		root = null;
	}

	/////////////////////////////////////////////////
	public void printPreOrder() {
		prPreOrder(root);
	}
	public void prPreOrder(TreeNode<T> tNode) {
		if (tNode != null) {
			System.out.println(tNode.key);
			prPreOrder(tNode.left);
			prPreOrder(tNode.right);
		}
	}

	public void printInOrder() {
		prInOrder(root);
	}

	public void prInOrder(TreeNode<T> tNode) {
		if (tNode != null) {
			prInOrder(tNode.left);
			System.out.println(tNode.key);
			prInOrder(tNode.right);
		}
	}

	public void printPostOrder() {
		prPostOrder(root);
	}

	public void prPostOrder(TreeNode<T> tNode) {
		if (tNode != null) {
			prPostOrder(tNode.left);
			prPostOrder(tNode.right);
			System.out.println(tNode.key);
		}
	}

	public T findMin() {
		if (root == null) {
			return null; // Or throw an exception for an empty tree
		}
		return findMinItem(root).key;
	}

	private TreeNode<T> findMinItem(TreeNode<T> tNode) {
		if (tNode.left == null) {
			return tNode;
		} else {
			return findMinItem(tNode.left);
		}
	}

	public T findMax() {
		if (root == null) {
			return null; // Or throw an exception for an empty tree
		}
		return findMaxItem(root).key;
	}

	private TreeNode<T> findMaxItem(TreeNode<T> tNode) {
		if (tNode.right == null) {
			return tNode;
		} else {
			return findMaxItem(tNode.right);
		}
	}
} // 코드 10-3
