package BST;

public interface IndexInterface<T extends Comparable<T>, N> {
	public N search(T x);
	public void insert(T x);
	public void delete(T x);
	public boolean isEmpty();
	public void clear();
} // 코드 10-1