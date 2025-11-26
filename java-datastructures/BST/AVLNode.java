package BST;

public class AVLNode<T extends Comparable<T>> {
	public T item;
	public AVLNode<T> left, right;
	public int height;
	
	public AVLNode(T newItem) {
		item = newItem;
		left = right = AVLTree.NIL; 
		height = 1;
	}
	
	public AVLNode(T newItem, AVLNode<T> leftChild, AVLNode<T> rightChild) {
		item = newItem;
		left = leftChild; right = rightChild;
		height = 1;
	}
	
	public AVLNode(T newItem, AVLNode<T> leftChild, AVLNode<T> rightChild, int h) {
		item = newItem;
		left = leftChild; right = rightChild;
		height = h;
	}
} // 코드 11-1