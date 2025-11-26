package hashTable;
import list.LinkedList;
import list.Node;
import BST.IndexInterface;

public class ChainedHashTable implements IndexInterface<Node> {	
	private LinkedList<Integer>[] table;
	int numItems = 0;
	
	public ChainedHashTable(int n) {
		table = (LinkedList<Integer>[]) new LinkedList[n]; 
		for (int i = 0; i < n; i++)
			table[i] = new LinkedList<>();
		numItems = 0;
	} 
	
	private int hash(Integer x) {                         
		return x % table.length; // 간단한 예
	}
	
	// [알고리즘 12-1] 구현: 검색, 삽입, 삭제
	public void insert(Integer x) {	
		int slot = hash(x);
		table[slot].add(0, x);
		numItems++;
	}
	
	public Node search(Integer x) {
		int slot = hash(x);
		if (table[slot].isEmpty()) return null; // null list 
		else {
			int i = table[slot].indexOf(x);
			if (i == Linkedlist.NOT_FOUND) return null; // not exist
			else return table[slot].getNode(i); 
		}
	}
	
	public void delete(Integer x) {
		if (isEmpty()) { /* 에러 처리 */ }
		else {
			int slot = hash(x);
			table[slot].removeItem(x);
			numItems--;
		}
	}
	
	// 기타
	public boolean isEmpty() {
		return numItems == 0;
	}
	
	public void clear() {
		for (int i = 0; i < table.length; i++)
			table[i] = new LinkedList<>(); 
		numItems = 0;
	}

	public void printAll() {
		for (int i=0; i < table.length; i++) {
			System.out.print("Slot " + i + " : ");
			table[i].printAll();
		}
	}
} // 코드 12-1