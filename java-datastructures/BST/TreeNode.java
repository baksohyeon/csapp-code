package BST;

public class TreeNode<T extends Comparable<T>> {
	public T key;
	public TreeNode<T> left;
	public TreeNode<T> right;
	public TreeNode(T newKey) {
		key = newKey;
		left = right = null;
	}
	public TreeNode(T newKey, TreeNode<T> leftChild, TreeNode<T> rightChild) {
		key = newKey;
		left = leftChild; right = rightChild;
	}
} // 코드 10-2