package BST;

public class BinarySearchTree implements IndexInterface<TreeNode> {
	private TreeNode root;
	public BinarySearchTree() { // 생성자
		root = null;
	}
	
	// [알고리즘 10-1] 구현 : 검색
	public TreeNode search(Comparable searchKey) {
		return searchItem(root, searchKey);
	}
	
	private TreeNode searchItem(TreeNode tNode, Comparable searchKey) {
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
	public void insert(Comparable newKey) {
		root = insertItem(root, newKey);
	}
 
	private TreeNode insertItem(TreeNode tNode, Comparable newItem) {
		if (tNode == null)  // insert after a leaf  (or into an empty tree)
			tNode = new TreeNode(newItem, null, null);
		else  if (newItem.compareTo(tNode.key) < 0) 	// branch left
			tNode.left = insertItem(tNode.left, newItem);
		else  					// branch right
			tNode.right = insertItem(tNode.right, newItem);
		return tNode; 
	}
	
	// [알고리즘 10-3] 구현 : 삭제
	public void delete(Comparable searchKey) {
		root = deleteItem(root, searchKey);
	}
	
	private TreeNode deleteItem(TreeNode tNode, Comparable searchKey) {
		if (tNode == null) return null;	// key not found!
		else {
			if (searchKey == tNode.key) 	// key found at tNode
				tNode = deleteNode(tNode);
			else if (searchKey.compareTo(tNode.key) < 0) 
				tNode.left = deleteItem(tNode.left, searchKey);
			else 
				tNode.right = deleteItem(tNode.right, searchKey);
			return tNode;
		}
	}
 
	private TreeNode deleteNode(TreeNode tNode) {
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
	
	private returnPair deleteMinItem(TreeNode tNode) {
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
		private Comparable key;
		private TreeNode node;
		private returnPair(Comparable it, TreeNode nd) {
			key = it;
			node = nd;
		}
	}
	
	// 기타
	public void isEmpty() {
		return root == null;
	}
	
	public void clear() {
		root = null;
	}

	/////////////////////////////////////////////////
	public void printPreOrder() {
		prPreOrder(root);
	}
	public void prPreOrder(TreeNode tNode) {
		if (tNode != null) {
			System.out.println(tNode.key);
			prPreOrder(tNode.left);
			prPreOrder(tNode.right);
		}
	}
} // 코드 10-3
