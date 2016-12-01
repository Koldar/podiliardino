package com.massimobono.podiliardino.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * An {@link ObservableList} where you can add no "null" and no duplicates entries
 * 
 * @author massi
 *
 */
public class ObservableDistinctList<E> implements ObservableList<E>{

	private ObservableList<E> underlyingList;
	
	public ObservableDistinctList(ObservableList<E> lists) {
		this.underlyingList = lists;
	}

	@Override
	public boolean add(E e) {
		if (e == null) {
			return false;
		}
		if (this.underlyingList.contains(e)){
			return false;
			
		}
		return this.underlyingList.add(e);
	}

	@Override
	public void add(int index, E element) {
		if (element == null) {
			return;
		}
		if (this.underlyingList.contains(element)) {
			return;
		}
		this.underlyingList.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return this.underlyingList.addAll(c.stream().filter(e -> e != null && !this.underlyingList.contains(e)).collect(Collectors.toList()));
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return this.underlyingList.addAll(index, c.stream().filter(e -> e != null && !this.underlyingList.contains(e)).collect(Collectors.toList()));
	}

	@Override
	public void clear() {
		this.underlyingList.clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.underlyingList.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.underlyingList.containsAll(c);
	}

	@Override
	public E get(int index) {
		return this.underlyingList.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return this.underlyingList.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return this.underlyingList.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {
		return this.underlyingList.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return this.underlyingList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return this.underlyingList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return this.underlyingList.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		return this.underlyingList.remove(o);
	}

	@Override
	public E remove(int index) {
		return this.underlyingList.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.underlyingList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.underlyingList.retainAll(c);
	}

	@Override
	public E set(int index, E element) {
		return this.underlyingList.set(index, element);
	}

	@Override
	public int size() {
		return this.underlyingList.size();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return this.underlyingList.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return this.underlyingList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.underlyingList.toArray(a);
	}

	@Override
	public void addListener(InvalidationListener listener) {
		this.underlyingList.addListener(listener);
	}

	@Override
	public void removeListener(InvalidationListener listener) {
		this.underlyingList.removeListener(listener);
	}

	@Override
	public void addListener(ListChangeListener<? super E> listener) {
		this.underlyingList.addListener(listener);
	}

	@Override
	public void removeListener(ListChangeListener<? super E> listener) {
		this.underlyingList.removeListener(listener);
	}

	@Override
	public boolean addAll(E... elements) {
		return this.underlyingList.addAll(elements);
	}

	@Override
	public boolean setAll(E... elements) {
		return this.underlyingList.setAll(elements);
	}

	@Override
	public boolean setAll(Collection<? extends E> col) {
		return this.underlyingList.setAll(col);
	}

	@Override
	public boolean removeAll(E... elements) {
		return this.underlyingList.removeAll(elements);
	}

	@Override
	public boolean retainAll(E... elements) {
		return this.underlyingList.retainAll(elements);
	}

	@Override
	public void remove(int from, int to) {
		this.underlyingList.remove(from, to);
	}

}
