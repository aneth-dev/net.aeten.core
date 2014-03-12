package net.aeten.core.util;

import java.util.Comparator;

import net.aeten.core.Factory;

public class AtomicArrayByComparator<E> implements
		AtomicArray <E> {
	private final AtomicValue <E>[] elements;

	@SuppressWarnings ("unchecked")
	public AtomicArrayByComparator (	Comparator <E> comparator,
												E[] array) {
		elements = new AtomicValue[array.length];
		for (int i = 0; i < array.length; ++i) {
			elements[i] = new AtomicValueByComparator <E> (comparator, array[i]);
		}
	}

	@SuppressWarnings ("unchecked")
	public AtomicArrayByComparator (	Comparator <E> comparator,
												int length) {
		elements = new AtomicValue[length];
		for (int i = 0; i < length; ++i) {
			elements[i] = new AtomicValueByComparator <E> (comparator, null);
		}
	}

	@Override
	public int length () {
		return elements.length;
	}

	@Override
	public E get (int i) {
		return elements[i].get ();
	}

	@Override
	public void set (	int i,
							E newValue) {
		elements[i].set (newValue);
	}

	@Override
	public void lazySet (int i,
								E newValue) {
		elements[i].lazySet (newValue);
	}

	@Override
	public E getAndSet (	int i,
								E newValue) {
		return elements[i].getAndSet (newValue);
	}

	@Override
	public boolean compareAndSet (int i,
											E expect,
											E update) {
		return elements[i].compareAndSet (expect, update);
	}

	@Override
	public boolean compareAndSet (int i,
											E expect,
											Factory <E, Void> update) {
		return elements[i].compareAndSet (expect, update);
	}

	@Override
	public boolean weakCompareAndSet (	int i,
													E expect,
													E update) {
		return elements[i].weakCompareAndSet (expect, update);
	}

	@Override
	public boolean weakCompareAndSet (	int i,
													E expect,
													Factory <E, Void> update) {
		return elements[i].weakCompareAndSet (expect, update);
	}

}
