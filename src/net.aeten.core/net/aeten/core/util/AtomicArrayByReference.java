package net.aeten.core.util;

import java.util.concurrent.atomic.AtomicReferenceArray;

import net.aeten.core.Factory;

public class AtomicArrayByReference<E> implements
		AtomicArray <E> {
	private final AtomicReferenceArray <E> referenceArray;

	public AtomicArrayByReference (E[] array) {
		referenceArray = new AtomicReferenceArray <E> (array);
	}

	public AtomicArrayByReference (int length) {
		referenceArray = new AtomicReferenceArray <E> (length);
	}

	@Override
	public int length () {
		return referenceArray.length ();
	}

	@Override
	public E get (int i) {
		return referenceArray.get (i);
	}

	@Override
	public void set (	int i,
							E newValue) {
		referenceArray.set (i, newValue);
	}

	@Override
	public void lazySet (int i,
								E newValue) {
		referenceArray.lazySet (i, newValue);
	}

	@Override
	public E getAndSet (	int i,
								E newValue) {
		return referenceArray.getAndSet (i, newValue);
	}

	@Override
	public boolean compareAndSet (int i,
											E expect,
											E update) {
		return referenceArray.compareAndSet (i, expect, update);
	}

	@Override
	public boolean compareAndSet (int i,
											E expect,
											Factory <E, Void> update) {
		return referenceArray.compareAndSet (i, expect, update.create (null));
	}

	@Override
	public boolean weakCompareAndSet (	int i,
													E expect,
													E update) {
		return referenceArray.weakCompareAndSet (i, expect, update);
	}

	@Override
	public boolean weakCompareAndSet (	int i,
													E expect,
													Factory <E, Void> update) {
		return referenceArray.weakCompareAndSet (i, expect, update.create (null));
	}

}
