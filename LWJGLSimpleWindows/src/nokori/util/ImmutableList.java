package nokori.util;

import java.util.ArrayList;

public class ImmutableList<E> {
	private ArrayList<E> internalList;
	
	public ImmutableList(ArrayList<E> internalList){
		this.internalList = internalList;
	}
	
	public E get(int index) {
		return internalList.get(index);
	}
	
	public int size() {
		return internalList.size();
	}
}
