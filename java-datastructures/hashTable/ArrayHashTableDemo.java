package hashTable;

import list.Node;

public class ArrayHashTableDemo {

	public static void main(String[] args) {
		System.out.println("Array Hash Table!");
		ArrayHashTable h = new ArrayHashTable(17); 

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
		h.insert(17);
		h.delete(40);
		System.out.println("  40 Deleted!");
		h.delete(17);
		System.out.println("  17 Deleted!");
		h.printAll();
		
		Integer key = 755;
		Integer slot = h.search(key);
		if (slot == ArrayHashTable.NOT_FOUND)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + h.getItem(slot));
		key = 75;
		slot = h.search(key);
		if (slot == ArrayHashTable.NOT_FOUND)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + h.getItem(slot));
		key = 38;
		slot = h.search(key);
		if (slot == ArrayHashTable.NOT_FOUND)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + h.getItem(slot));
		key = 5;
		slot = h.search(key);
		if (slot == ArrayHashTable.NOT_FOUND)
			System.out.println("Search Failed for " + key);
		else 
			System.out.println("Successful search for " + h.getItem(slot));
	}

}
