package list;
// 제네릭으로 수정하던 중
public class LinkedListIteratorDemo {

	public LinkedListIteratorDemo() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			
			LinkedList<Integer> list1 = new LinkedList<>(); 
			list1.add(0,300);
			list1.add(0, 200);
			list1.add(0, 100);
			list1.append(500);
			list1.append(600);
			list1.add(3, 250);
			list1.add(1, 50);
			list1.add(0, 10);
			list1.append(700);
			list1.add(9, 800); list1.printAll();
			System.out.println("Iterator example: Linked List!");
			LinkedListIterator iter = new LinkedListIterator(list1);
			while(iter.hasNext()) 
				System.out.println("next() = " + iter.next().item);
			LinkedList<Integer> list2 = new LinkedList<>(); 
			list2.add(0, -300);
			list2.add(0, 200);
			list2.add(0, -100);
			list2.append(-500);
			list2.append(600);
			list2.add(3, 250);
			list2.add(1, 50);
			list2.add(0, -10);
			list2.append(700);
			list2.add(9, -800); list2.printAll();
			System.out.println("Item Iterator example: Linked List!");
			LinkedListItemIterator iter2 = new LinkedListItemIterator(list2);
			while(iter2.hasNext()) 
				System.out.println("next() = " + iter2.next());
			list1.printAll();
			System.out.println("Negative Item Iterator example: Linked List!");
			NegativeItemIterator<Integer> iter3 = new NegativeItemIterator<>(list2);
			int tmpcnt = 0;
			while(iter3.hasNext()) {
				System.out.println("next() = " + iter3.next());
				if (++tmpcnt > 20) break;
			}
			list1.printAll();  
	}
}
