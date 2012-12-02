package net.aeten.core.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

import net.aeten.core.Factory;

public final class AtomicValueByReference<V> implements
		AtomicValue <V>,
		Serializable {
	private static final long serialVersionUID = 3931680837605611661L;
	private final AtomicReference <V> reference;

	public AtomicValueByReference (V initialValue) {
		reference = new AtomicReference <V> (initialValue);
	}

	@Override
	public boolean compareAndSet (V expect,
											V update) {
		return reference.compareAndSet (expect, update);
	}

	@Override
	public V get () {
		return reference.get ();
	}

	@Override
	public V getAndSet (V newValue) {
		return reference.getAndSet (newValue);
	}

	@Override
	public void lazySet (V newValue) {
		reference.lazySet (newValue);
	}

	@Override
	public void set (V newValue) {
		reference.set (newValue);
	}

	@Override
	public boolean weakCompareAndSet (	V expect,
													V update) {
		return reference.weakCompareAndSet (expect, update);
	}

	@Override
	public final String toString () {
		return String.valueOf (get ());
	}

	@Override
	public boolean compareAndSet (V expect,
											Factory <V, Void> update) {
		return reference.compareAndSet (expect, update.create (null));
	}

	@Override
	public boolean weakCompareAndSet (	V expect,
													Factory <V, Void> update) {
		return reference.weakCompareAndSet (expect, update.create (null));
	}

}