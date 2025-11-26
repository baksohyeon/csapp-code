package hashTable;

import list.Node;

public class ChainedHashTableDemo {
	public static void main(String[] args) {
		System.out.println("ChainedHashTable!~!!");
		ChainedHashTable h = new ChainedHashTable(11); 
		h.insert(10);
		h.insert(20);
		h.insert(5);
		h.insert(80);
		h.insert(90);
		h.insert(75);
		h.insert(86);
		h.insert(30);
		h.insert(77);
		h.insert(15);
		h.insert(40);
		h.insert(38);
		h.insert(49);
		System.out.println("`13 items inserted!");
		h.printAll();
		h.delete(75);
		System.out.println("  75 Deleted!");
		h.delete(40);
		System.out.println("  40 Deleted!");
		h.printAll();
		
		int key = 755;
		Node<Integer> nd = h.search(key);
		if (nd == null)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + nd.item);
		key = 75; nd = h.search(key);
		if (nd == null)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + nd.item);
		key = 38; nd = h.search(key);
		if (nd == null)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + nd.item);		
		key = 5; nd = h.search(key);
		if (nd == null)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + nd.item);
	}
} 
