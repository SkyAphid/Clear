package nokori.util;

import java.io.Serializable;

public class FastArrayList<E> implements Serializable{
	
	private static final long serialVersionUID = -3570017818041447411L;

	private static final int MIN_FULL_CLEAR_INTERVAL = 100, MAX_FULL_CLEAR_INTERVAL = 200;
	
	private int fullClearInterval;
	
	private int capacity;
	private E[] elements;

	private int size, maxSize;
	//@SerializeLength
	private int fullClearTimer;
	
	public FastArrayList() {
		this(10);
	}
	
	@SuppressWarnings("unchecked")
	public FastArrayList(int initialCapacity) {
		
		fullClearInterval = MIN_FULL_CLEAR_INTERVAL + (int)((MAX_FULL_CLEAR_INTERVAL + 1 - MIN_FULL_CLEAR_INTERVAL) * Math.random());
		
		capacity = initialCapacity;
		elements = (E[]) new Object[capacity];
		
		size = 0;
		fullClearTimer = fullClearInterval;
	}
	
	public void add(E element){
		if(size == capacity){
			expand(capacity+1);
		}
		elements[size++] = element;
	}
	
	public void add(FastArrayList<E> list){
		
		if(list.isEmpty()){
			return;
		}
		
		int newSize = size + list.size;
		if(newSize > capacity){
			expand(newSize);
		}
		
		System.arraycopy(list.elements, 0, elements, size, list.size);
		size = newSize;
	}
	
	public int indexOf(E e){
		for (int i = 0; i < elements.length; i++){
			if (elements[i] == e) return i;
		}
		return -1;
	}
	
	public boolean contains(E e){
		for (int i = 0; i < elements.length; i++){
			if (elements[i] == e) return true;
		}
		
		return false;
	}
	
	public boolean containsEquals(E e){
		for (int i = 0; i < elements.length; i++){
			if (elements[i].equals(e)) return true;
		}
		
		return false;
	}
	
	public void trimToSize(){
		if(capacity > size){
			
			capacity = size;
			
			@SuppressWarnings("unchecked")
			E[] newElements = (E[]) new Object[capacity];
			System.arraycopy(elements, 0, newElements, 0, capacity);
			elements = newElements;
		}
	}
	
	public boolean isEmpty(){
		return size == 0;
	}
	
	public int size(){
		return size;
	}
	
	public E get(int i){
		return elements[i];
	}
	
	public E removeLast(){
		return elements[--size];
	}
	
	public E replace(int index, E newElement){
		E old = elements[index];
		elements[index] = newElement;
		return old;
	}

	public int takeFrom(FastArrayList<E> list, int count) {
		count = Math.min(count, list.size);
		ensureCapacity(count);
		System.arraycopy(list.elements, list.size - count, elements, size, count);
		
		list.size -= count;
		size += count;
		
		return count;
	}
	
	public E removeSwap(int index){
		E result = elements[index];
		elements[index] = elements[--size];
		return result;
	}
	
	public void forceClear(){
		fullClearTimer = 0;
		clear();
	}
	
	public void clear(){
		if(--fullClearTimer < 0){
			for(int i = 0; i < maxSize; i++){
				elements[i] = null;
			}
			fullClearTimer = fullClearInterval;
			maxSize = 0;
		}else{
			maxSize = Math.max(size, maxSize);
		}
		size = 0;
	}
	
	public void reset() {
		size = 0;
	}
	
	public E[] getUnderlyingArray(){
		return elements;
	}
	
	public void ensureCapacity(int minCapacity){
		if(capacity < minCapacity){
			expand(minCapacity);
		}
	}

	@SuppressWarnings("unchecked")
	private void expand(int minCapacity) {
		E[] oldElements = elements;
		capacity = Math.max(minCapacity, capacity*2);
		/*if(capacity > 1024*16){
			System.out.println("FAL resized to " + capacity);
		}*/
		elements = (E[]) new Object[capacity];
		System.arraycopy(oldElements, 0, elements, 0, size);
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder("[");
		for(int i = 0; i < size; i++){
			b.append(elements[i]);
			if(i != size-1){
				b.append(", ");
			}
		}
		return b.append(']').toString();
	}
}
