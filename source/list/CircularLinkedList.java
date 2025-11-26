package list;

public class CircularLinkedList<E> implements ListInterface<E> {
	private Node<E> tail;
	private int numItems;

	public CircularLinkedList() {		// 생성자
		numItems = 0;
		tail = new Node(-1);
		tail.next = tail;
	}

	public void add(int index, E x) {	// 첫 번째 원소는 0번 원소
		if (index >= 0 && index <= numItems) {
			Node<E> prevNode = getNode(index - 1);
			Node<E> newNode = new Node(x, prevNode.next);
			prevNode.next = newNode;
			if (index == numItems)
				tail = newNode;
			numItems++;
		}
	}

	public void append(E x) {
		Node<E> prevNode = tail;		// 더미 노드
		Node<E> newNode = new Node(x, tail.next);
		prevNode.next = newNode;
		tail = newNode;
		numItems++;
	}

	public E remove(int index) {
		if (index >= 0 && index <= numItems-1) {
			Node<E> prevNode = getNode(index - 1);
			E rItem = prevNode.next.item;
			prevNode.next = prevNode.next.next;
			if (index == numItems)
				tail = prevNode;
			numItems--;
			return rItem;
		} else return null;
	}

	public boolean removeItem(E x) {	
		Node<E> currNode = tail.next; 	// 더미 헤드
		Node<E> prevNode;
		for (int i = 0; i < numItems; i++) {
			prevNode = currNode;
			currNode = currNode.next;
			if (((Comparable)(currNode.item)).compareTo(x) == 0) {
				prevNode.next = currNode.next;
				numItems--; 
				return true;
			}	
		}
		return false; // not found
	}

	public E get(int index) {
		if (index >= 0 && index <= numItems-1) {
			return getNode(index).item; 
		} else return null;	// 에러
	}


	public void set(int index, E x) {
		if (index >= 0 && index <= numItems-1) {
			getNode(index).item = x; 
		} else { /* 에러 처리 */ }
	}

	private Node<E> getNode(int index) {
		if (index >= -1 && index <= numItems) {
			Node<E> currNode = tail.next;  // 더미 헤드
			for (int i = 0; i <= index; i++) {
				currNode = currNode.next;
			}
			return currNode; 
		} else {return null;}
	}

	public final int NOT_FOUND = -12345;
	public int indexOf(E x) {		// 원소 x의 인덱스 리턴
		Node<E> currNode = tail.next;	// 더미 헤드
		for (int i = 0; i <= numItems-1; i++) {
			currNode = currNode.next;
			if (((Comparable)(currNode.item)).compareTo(x) == 0) return i;
		}
		return NOT_FOUND; 
	}

	public int len() {
		return numItems;
	}

	public boolean isEmpty() { 
		return numItems == 0;
	}

	public void clear() {
		numItems = 0;
		tail = new Node(-1);
		tail.next = tail;
	 }

	 ///////////////////////////////////////////////////////////////////////
	 public void printAll() {
		Node<E> head = tail.next; // 더미 헤드
		System.out.print("Print list (#items=" + numItems + ") ");
		for(Node<E> t=head.next; t != head; t = t.next)
			System.out.print(t.item + " ");
		System.out.println();
	}
} // 코드 5-11
