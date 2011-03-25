package se.panamach.util.datastructure;

import java.util.ArrayList;
import java.util.Collection;

public class FixedSizeList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 2503907725326027336L;

	private int maxSize;
	
	public FixedSizeList(int size) {
		super(size);
		this.maxSize = size;
	}

	public T getLastElement() {
		if (this.size() == 0)
			return null;
		
		return this.get(this.size()-1);
	}
	
	private void removeFirst() {
		this.remove(0);
	}
		
	@Override
	public void add(int index, T element) {
		if (index > maxSize) {
			throw new IllegalArgumentException("Index out of range, " + index + ">" + maxSize);
		}
		
		if (this.size() == maxSize) {
			removeFirst();
		}
		
		super.add(index, element);
	}

	@Override
	public boolean add(T element) {
		if (this.size() == maxSize) {
			removeFirst();
		}
		
		return super.add(element);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		if (collection.size() > maxSize) {
			throw new IllegalArgumentException("Collection size out of range, " + collection.size() + ">" + maxSize);
		}
		return super.addAll(collection);
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> collection) {
		if (collection.size()+index > maxSize) {
			throw new IllegalArgumentException("Collection size out of range, " + collection.size() + ">" + maxSize);
		}
		return super.addAll(index, collection);
	}

	@Override
	public T set(int index, T element) {
		if (index > maxSize)
			throw new IllegalArgumentException("Index out of range, " + index + ">" + maxSize);
		
		if (this.size() == maxSize) {
			removeFirst();
		}
		
		return super.set(index, element);
	}

	public T getFirstElement() {
		if (this.size() == 0)
			return null;
		
		return get(0);
	}
	
	
}
