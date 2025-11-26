package BST;

public class BinarySearchTreeDemo {
	public static void main(String[] args) {
		System.out.println("Binary Search Tree!");
		BinarySearchTree bst1 = new BinarySearchTree(); 
		bst1.insert(10);
		bst1.insert(20);
		bst1.insert(5);
		bst1.insert(80);
		bst1.insert(90);
		bst1.insert(75);
		bst1.insert(301);
		bst1.insert(77);
		bst1.insert(15);
		bst1.insert(40);
		bst1.printPreOrder();
		Integer del1 = 20;
		bst1.delete(del1); System.out.println(del1 + " Deleted!");
		bst1.printPreOrder();
		Integer del2 = 20;
		bst1.delete(del2); System.out.println(del2 + " Deleted!");
		bst1.printPreOrder();
		
		Integer tmp = 755;
		TreeNode res = bst1.search(tmp);
		if(res ==  null) {
			System.out.println("Search for " + tmp + " failed" );
		} else {
			System.out.println("Search for " + tmp + " returned the node of " + res.key);	
		}
		tmp = 5;
		res = bst1.search(tmp);
		if(res ==  null) {
				System.out.println("Search for " + tmp + " Failed");
		} else {
			System.out.println("Search for " + tmp + " returned the node of " + res.key);
		}
	}
} // 코드 10-4
